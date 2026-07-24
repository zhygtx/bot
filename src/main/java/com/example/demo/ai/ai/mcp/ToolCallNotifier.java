package com.example.demo.ai.ai.mcp;

import com.example.demo.ai.ai.pojo.entity.AIToolCallRecord;
import com.example.demo.ai.ai.service.AIToolCallRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * 工具调用通知器。
 * 只做三件事：追加 AI 回复分段、推送工具调用事件、保存工具调用记录。
 *
 * <p>前端通知协议：所有工具事件的结果统一为 {@code {success, summary, detail}}，
 * 前端无需根据工具类型分别处理。</p>
 */
@Component
public class ToolCallNotifier {

    private final ThreadLocal<Session> currentSession = new ThreadLocal<>();
    private final AIToolCallRecordService toolCallRecordService;
    private final ObjectMapper objectMapper;

    public ToolCallNotifier(AIToolCallRecordService toolCallRecordService, ObjectMapper objectMapper) {
        this.toolCallRecordService = toolCallRecordService;
        this.objectMapper = objectMapper;
    }

    /**
     * 开始一轮 AI 回复通知。
     */
    public void startSession(String conversationId, String assistantMessageId, Integer round,
                             List<Map<String, Object>> messageParts,
                             BiConsumer<String, Object> onEvent) {
        currentSession.set(new Session(conversationId, assistantMessageId, round, messageParts, onEvent));
    }

    /**
     * 结束当前线程上的 AI 回复通知。
     */
    public void closeSession() {
        currentSession.remove();
    }

    /**
     * 追加 AI 文本回复并推送增量事件。
     */
    public void appendAssistantText(String text) {
        Session session = currentSession.get();
        if (session == null || text == null || text.isEmpty()) {
            return;
        }
        for (int i = 0; i < text.length(); i += 8) {
            int end = Math.min(i + 8, text.length());
            String chunk = text.substring(i, end);
            int partIndex = session.addTextPart(chunk);
            session.onEvent.accept("assistant_text_delta", Map.of(
                    "messageId", session.assistantMessageId,
                    "conversationId", session.conversationId,
                    "round", session.round,
                    "partIndex", partIndex,
                    "content", chunk
            ));
        }
    }

    /**
     * 执行一次工具调用：先通知开始，再执行业务方法，最后通过工具自带的 resultHandler 转换结果并通知。
     */
    public <T> T call(ToolNotice notice, Supplier<T> action) {
        Session session = currentSession.get();
        if (session == null) {
            return action.get();
        }

        String toolCallId = UUID.randomUUID().toString();
        int sequence = session.nextSequence();
        int partIndex = session.addToolPart(toolCallId);
        LocalDateTime startedAt = LocalDateTime.now();

        AIToolCallRecord record = AIToolCallRecord.builder()
                .id(toolCallId)
                .conversationId(session.conversationId)
                .assistantMessageId(session.assistantMessageId)
                .round(session.round)
                .sequence(sequence)
                .partIndex(partIndex)
                .method(notice.method())
                .displayName(notice.displayName())
                .description(notice.description())
                .category(notice.category())
                .argumentsPreviewJson(toJson(notice.argumentsPreview()))
                .status("RUNNING")
                .startedAt(startedAt)
                .build();
        toolCallRecordService.save(record);
        session.onEvent.accept("tool_call_start",
                buildPayload(record, notice.argumentsPreview(), null));

        try {
            T result = action.get();
            ToolNotice.ResultSummary summary = notice.resultHandler().handle(result);
            finish(record, summary, startedAt, session);
            return result;
        } catch (RuntimeException | Error error) {
            ToolNotice.ResultSummary summary = ToolNotice.ResultSummary.error(error);
            fail(record, summary, startedAt, session);
            throw error;
        }
    }

    private void finish(AIToolCallRecord record, ToolNotice.ResultSummary summary,
                        LocalDateTime startedAt, Session session) {
        LocalDateTime finishedAt = LocalDateTime.now();
        record.setStatus(summary.success() ? "SUCCESS" : "ERROR");
        record.setResultPreviewJson(toJson(toResultMap(summary)));
        record.setFinishedAt(finishedAt);
        record.setDurationMs(Duration.between(startedAt, finishedAt).toMillis());
        toolCallRecordService.updateById(record);
        session.onEvent.accept(summary.success() ? "tool_call_finish" : "tool_call_error",
                buildPayload(record, null, summary));
    }

    private void fail(AIToolCallRecord record, ToolNotice.ResultSummary summary,
                      LocalDateTime startedAt, Session session) {
        LocalDateTime finishedAt = LocalDateTime.now();
        record.setStatus("ERROR");
        record.setErrorMessage(summary.summary());
        record.setResultPreviewJson(toJson(toResultMap(summary)));
        record.setFinishedAt(finishedAt);
        record.setDurationMs(Duration.between(startedAt, finishedAt).toMillis());
        toolCallRecordService.updateById(record);
        session.onEvent.accept("tool_call_error",
                buildPayload(record, null, summary));
    }

    /**
     * 构建推送给前端的统一事件 payload。
     */
    private Map<String, Object> buildPayload(AIToolCallRecord record,
                                              List<Map<String, Object>> argumentsPreview,
                                              ToolNotice.ResultSummary resultSummary) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("toolCallId", record.getId());
        payload.put("conversationId", record.getConversationId());
        payload.put("messageId", record.getAssistantMessageId());
        payload.put("status", record.getStatus());
        payload.put("tool", Map.of(
                "name", record.getMethod(),
                "displayName", record.getDisplayName(),
                "description", record.getDescription(),
                "category", record.getCategory()
        ));
        payload.put("arguments", argumentsPreview != null ? argumentsPreview : List.of());
        if (resultSummary != null) {
            payload.put("result", toResultMap(resultSummary));
        }
        payload.put("errorMessage", record.getErrorMessage());
        payload.put("durationMs", record.getDurationMs());
        return payload;
    }

    private static Map<String, Object> toResultMap(ToolNotice.ResultSummary summary) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("success", summary.success());
        map.put("summary", summary.summary());
        map.put("detail", summary.detail());
        return map;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "{}";
        }
    }

    /**
     * 当前 AI 回复的通知上下文。
     */
    private static final class Session {
        private final String conversationId;
        private final String assistantMessageId;
        private final Integer round;
        private final List<Map<String, Object>> messageParts;
        private final BiConsumer<String, Object> onEvent;
        private int sequence = 0;

        private Session(String conversationId, String assistantMessageId, Integer round,
                        List<Map<String, Object>> messageParts, BiConsumer<String, Object> onEvent) {
            this.conversationId = conversationId;
            this.assistantMessageId = assistantMessageId;
            this.round = round;
            this.messageParts = messageParts;
            this.onEvent = onEvent;
        }

        private synchronized int nextSequence() {
            return ++sequence;
        }

        private synchronized int addTextPart(String chunk) {
            if (!messageParts.isEmpty()) {
                Map<String, Object> lastPart = messageParts.get(messageParts.size() - 1);
                if ("text".equals(lastPart.get("type"))) {
                    lastPart.put("content", lastPart.getOrDefault("content", "") + chunk);
                    return messageParts.size() - 1;
                }
            }
            Map<String, Object> part = new LinkedHashMap<>();
            part.put("type", "text");
            part.put("content", chunk);
            messageParts.add(part);
            return messageParts.size() - 1;
        }

        private synchronized int addToolPart(String toolCallId) {
            Map<String, Object> part = new LinkedHashMap<>();
            part.put("type", "tool_call");
            part.put("toolCallId", toolCallId);
            messageParts.add(part);
            return messageParts.size() - 1;
        }
    }
}
