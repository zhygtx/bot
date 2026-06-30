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
 */
@Slf4j
@Component
public class AIPluginWebSocketHandler extends TextWebSocketHandler {

    private final AIPluginService aiPluginService;
    private final ObjectMapper objectMapper;
    private final Executor generationExecutor = Executors.newCachedThreadPool();
    private final Map<String, CompletableFuture<?>> tasks = new ConcurrentHashMap<>();

    public AIPluginWebSocketHandler(AIPluginService aiPluginService, ObjectMapper objectMapper) {
        this.aiPluginService = aiPluginService;
        this.objectMapper = objectMapper;
    }

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

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        cancelTask(session);
    }

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

    private void cancelTask(WebSocketSession session) {
        CompletableFuture<?> task = tasks.remove(session.getId());
        if (task != null) {
            task.cancel(true);
        }
    }

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

    private void closeQuietly(WebSocketSession session, CloseStatus status) {
        try {
            if (session.isOpen()) {
                session.close(status);
            }
        } catch (IOException ignored) {
            // ignore
        }
    }
}
