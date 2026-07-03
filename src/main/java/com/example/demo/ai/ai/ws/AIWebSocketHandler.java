package com.example.demo.ai.ai.ws;

import com.example.demo.ai.ai.pojo.entity.AIChatMessage;
import com.example.demo.ai.ai.service.AIService;
import com.example.demo.security.UserPrincipal;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class AIWebSocketHandler extends TextWebSocketHandler {

    private final AIService aiService;
    private final ObjectMapper objectMapper;

    private final Executor generationExecutor = Executors.newCachedThreadPool();
    private final Map<String, AtomicBoolean> cancelFlags = new ConcurrentHashMap<>();

    private final Map<String, Object> sendLocks = new ConcurrentHashMap<>();

    public AIWebSocketHandler(AIService aiService, ObjectMapper objectMapper) {
        this.aiService = aiService;
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
    protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws Exception {
        JsonNode root = objectMapper.readTree(message.getPayload());
        String type = root.path("type").asText();
        if ("message".equals(type)) {
            startGeneration(session, root.path("data"));
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
    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus status) {
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
        UserPrincipal user = (UserPrincipal) session.getAttributes().get(AIWsAuthInterceptor.ATTR_USER);
        if (user == null) {
            send(session, "error", Map.of("message", "登录状态已失效"));
            closeQuietly(session, CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        String conversationId = payloadNode.path("conversationId").asText();
        AtomicBoolean cancelled = new AtomicBoolean(false);
        cancelFlags.put(session.getId(), cancelled);

        generationExecutor.execute(() -> {
            try {
                List<AIChatMessage> messages = aiService.aiGenerate(
                        payloadNode.path("message").asText(), conversationId, user.userId(),
                        (type, data) -> send(session, type, data), cancelled);
                send(session, "done", messages);
            } catch (Exception e) {
                log.error("AI 生成任务执行异常", e);
                send(session, "error", Map.of("message", "AI 生成任务执行异常"));
            } finally {
                cancelFlags.remove(session.getId());
                closeQuietly(session, CloseStatus.NORMAL);
            }
        });
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
            Object lock = sendLocks.computeIfAbsent(session.getId(), id -> new Object());
            synchronized (lock) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(payload));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 取消指定会话的生成任务。
     * <p>
     * 从任务映射表中移除并尝试中断对应的异步任务。
     * 如果任务不存在，则不做任何操作。
     * @param session WebSocket 会话
     */
    private void cancelTask(WebSocketSession session) {
        AtomicBoolean cancelled = cancelFlags.remove(session.getId());
        if (cancelled != null) {
            cancelled.set(true);
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