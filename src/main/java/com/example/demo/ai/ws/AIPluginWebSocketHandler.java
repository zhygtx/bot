package com.example.demo.ai.ws;

import com.example.demo.ai.pojo.dto.GenerateRequest;
import com.example.demo.ai.pojo.dto.GenerateResponse;
import com.example.demo.ai.pojo.dto.StreamGenerateRequest;
import com.example.demo.ai.pojo.dto.TurnRequest;
import com.example.demo.ai.service.AIPluginService;
import com.example.demo.security.UserPrincipal;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * AI 插件代码生成 WebSocket 处理器。
 * <p>
 * 负责处理前端通过 WebSocket 发来的 AI 代码生成请求，支持两种核心操作：
 * <ul>
 *     <li><b>generate</b>：启动一个流式 AI 生成任务，将增量结果实时推送回客户端</li>
 *     <li><b>cancel</b>：取消当前会话正在执行的生成任务</li>
 * </ul>
 * 每个 WebSocket 会话同一时间只允许一个生成任务运行，新任务会自动取消旧任务。
 * 任务完成后（无论成功或失败），WebSocket 连接会被自动关闭。
 */
@Slf4j
@Component
public class AIPluginWebSocketHandler extends TextWebSocketHandler {

    /** AI 插件核心服务，负责实际的流式生成逻辑 */
    private final AIPluginService aiPluginService;

    /** JSON 序列化/反序列化工具 */
    private final ObjectMapper objectMapper;

    /** 缓存线程池，用于异步执行 AI 生成任务，避免阻塞 WebSocket I/O 线程 */
    private final Executor generationExecutor = Executors.newCachedThreadPool();

    /**
     * 当前活跃的生成任务映射表。
     * key 为 WebSocket 会话 ID，value 为对应的异步任务 Future。
     * 用于支持任务取消和会话级别的任务管理。
     */
    private final Map<String, CompletableFuture<?>> tasks = new ConcurrentHashMap<>();

    public AIPluginWebSocketHandler(AIPluginService aiPluginService, ObjectMapper objectMapper) {
        this.aiPluginService = aiPluginService;
        this.objectMapper = objectMapper;
    }

    /**
     * 处理客户端发来的文本消息。
     * <p>
     * 消息格式为 JSON，必须包含 {@code type} 字段：
     * <ul>
     *     <li>{@code "generate"}：启动生成任务，payload 中包含生成参数</li>
     *     <li>{@code "cancel"}：取消当前会话的生成任务</li>
     * </ul>
     *
     * @param session WebSocket 会话
     * @param message 客户端发来的文本消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode root = objectMapper.readTree(message.getPayload());
        String type = root.path("type").asText();
        if ("generate".equals(type)) {
            startGeneration(session, root.path("payload"));
            return;
        }
        if ("cancel".equals(type)) {
            cancelTask(session);
            send(session, "cancelled", null);
            return;
        }
        send(session, "error", Map.of("message", "未知消息类型: " + type));
    }

    /**
     * 连接关闭时的回调，确保清理该会话的活跃任务。
     *
     * @param session WebSocket 会话
     * @param status  关闭状态码
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        cancelTask(session);
    }

    /**
     * 启动流式 AI 生成任务。
     * <p>
     * 处理逻辑：
     * <ol>
     *     <li>取消当前会话已有的任务（如果有）</li>
     *     <li>校验用户登录状态，未登录则返回错误并关闭连接</li>
     *     <li>解析请求参数，判断是新建对话还是追加轮次</li>
     *     <li>异步执行生成任务，将增量结果实时推送给客户端</li>
     *     <li>任务完成后发送 {@code "done"} 消息并关闭连接</li>
     * </ol>
     *
     * @param session      WebSocket 会话
     * @param payloadNode  请求参数 JSON 节点
     */
    private void startGeneration(WebSocketSession session, JsonNode payloadNode) {
        cancelTask(session);
        UserPrincipal user = (UserPrincipal) session.getAttributes().get(AIPluginWsAuthInterceptor.ATTR_USER);
        if (user == null) {
            send(session, "error", Map.of("message", "登录状态已失效"));
            closeQuietly(session, CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        CompletableFuture<?> task = CompletableFuture.runAsync(() -> {
            try {
                StreamGenerateRequest request = objectMapper.treeToValue(payloadNode, StreamGenerateRequest.class);
                GenerateRequest startRequest = new GenerateRequest(
                        request.getRequirements(),
                        request.getEntityPackage(),
                        request.getMethodPackage(),
                        request.getPluginId());
                TurnRequest turnRequest = null;
                if (request.getConversationId() != null && !request.getConversationId().isBlank()) {
                    turnRequest = new TurnRequest(
                            request.getConversationId(),
                            request.getInstruction(),
                            request.getEntityPackage(),
                            request.getMethodPackage());
                    startRequest = null;
                }

                GenerateResponse response = aiPluginService.createStreamTurn(
                        startRequest,
                        turnRequest,
                        user.userId(),
                        delta -> send(session, "delta", delta)
                );
                send(session, "done", response);
            } catch (Exception e) {
                log.error("AI WebSocket 流式生成失败", e);
                send(session, "error", Map.of("message", e.getMessage() != null ? e.getMessage() : "AI 生成失败"));
            } finally {
                tasks.remove(session.getId());
                closeQuietly(session, CloseStatus.NORMAL);
            }
        }, generationExecutor);

        tasks.put(session.getId(), task);
    }

    /**
     * 取消指定会话的生成任务。
     * <p>
     * 从任务映射表中移除并尝试中断对应的异步任务。
     * 如果任务不存在，则不做任何操作。
     * @param session WebSocket 会话
     */
    private void cancelTask(WebSocketSession session) {
        CompletableFuture<?> task = tasks.remove(session.getId());
        if (task != null) {
            task.cancel(true);
        }
    }

    /**
     * 向客户端发送一条 JSON 格式的消息。
     * <p>
     * 消息结构为 {@code {"type": type, "data": data}}。
     * 发送前会检查会话是否仍然打开，并使用 {@code synchronized} 保证同一会话的消息串行发送，
     * 避免并发写入导致 WebSocket 协议异常。
     *
     * @param session WebSocket 会话
     * @param type    消息类型
     * @param data    消息数据，为 {@code null} 时发送空对象
     */
    private void send(WebSocketSession session, String type, Object data) {
        if (!session.isOpen()) return;
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "type", type,
                    "data", data == null ? Map.of() : data
            ));
            synchronized (session) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(payload));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 静默关闭 WebSocket 连接。
     * <p>
     * 忽略关闭过程中的异常，确保不会因连接已关闭等问题抛出异常中断上层流程。
     *
     * @param session WebSocket 会话
     * @param status  关闭状态码
     */
    private void closeQuietly(WebSocketSession session, CloseStatus status) {
        try {
            if (session.isOpen()) {
                session.close(status);
            }
        } catch (IOException ignored) {
            // 静默忽略关闭异常，连接可能已被客户端主动断开
        }
    }
}