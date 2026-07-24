package com.example.demo.ai.ai.mcp;

import com.example.demo.ai.ai.pojo.entity.AIToolCallRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * 工具调用通知器。
 * 只做两件事：推送 SSE 事件、收集工具调用记录到内存。
 *
 * <p><b>持久化由调用方负责：</b>流式完成后，{@code AIServiceImpl} 从 {@link AIStreamContext}
 * 读取快照，一次性批量写入 DB。本类不直接操作数据库。</p>
 *
 * <p><b>线程模型：</b>AI 流式生成的文本增量回调（{@code doOnNext}）和 Spring AI 的工具执行
 * 都运行在 Reactor/Netty 线程上，与发起会话的业务线程不同。这里用 {@link ConcurrentHashMap}
 * 按 {@code assistantMessageId} 索引上下文，由调用方显式传入。</p>
 */
@Slf4j
@Component
public class ToolCallNotifier {

    private final ConcurrentHashMap<String, AIStreamContext> contexts = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public ToolCallNotifier(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 开始一轮 AI 回复的流式收集。
     *
     * @return 创建的上下文，调用方在流式完成后用它读取快照进行持久化
     */
    public AIStreamContext startContext(String conversationId, String assistantMessageId, Integer round,
                                        BiConsumer<String, Object> onEvent) {
        AIStreamContext ctx = new AIStreamContext(conversationId, assistantMessageId, round, onEvent);
        contexts.put(assistantMessageId, ctx);
        return ctx;
    }

    /**
     * 结束指定消息的流式收集。
     */
    public void closeContext(String assistantMessageId) {
        if (assistantMessageId != null) {
            contexts.remove(assistantMessageId);
        }
    }

    /**
     * 追加 AI 文本回复并推送增量事件。
     */
    public void appendAssistantText(String assistantMessageId, String text) {
        AIStreamContext ctx = assistantMessageId == null ? null : contexts.get(assistantMessageId);
        if (ctx == null) {
            log.warn("appendAssistantText 找不到上下文，assistantMessageId={}, text 长度={}",
                    assistantMessageId, text == null ? 0 : text.length());
            return;
        }
        ctx.appendText(text);
    }

    /**
     * 执行一次工具调用：先推送开始事件，再执行业务方法，最后收集记录到内存。
     *
     * @param assistantMessageId 当前 AI 回复消息 ID，用于找回上下文；为 null 时直接执行不记录
     */
    public <T> T call(String assistantMessageId, ToolNotice notice, Supplier<T> action) {
        AIStreamContext ctx = assistantMessageId == null ? null : contexts.get(assistantMessageId);
        if (ctx == null) {
            return action.get();
        }

        String toolCallId = UUID.randomUUID().toString();
        int sequence = ctx.nextSequence();
        int partIndex = ctx.addToolPart(toolCallId);
        LocalDateTime startedAt = LocalDateTime.now();

        AIToolCallRecord record = AIToolCallRecord.builder()
                .id(toolCallId)
                .conversationId(ctx.getConversationId())
                .assistantMessageId(ctx.getAssistantMessageId())
                .round(ctx.getRound())
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
        ctx.addToolCallRecord(record);
        ctx.emitEvent("tool_call_start",
                buildPayload(record, notice.argumentsPreview(), null));

        try {
            T result = action.get();
            ToolNotice.ResultSummary summary = notice.resultHandler().handle(result);
            updateRecordOnFinish(record, summary, startedAt);
            ctx.emitEvent(summary.success() ? "tool_call_finish" : "tool_call_error",
                    buildPayload(record, null, summary));
            return result;
        } catch (RuntimeException | Error error) {
            ToolNotice.ResultSummary summary = ToolNotice.ResultSummary.error(error);
            updateRecordOnFail(record, summary, startedAt);
            ctx.emitEvent("tool_call_error", buildPayload(record, null, summary));
            throw error;
        }
    }

    private void updateRecordOnFinish(AIToolCallRecord record, ToolNotice.ResultSummary summary,
                                      LocalDateTime startedAt) {
        LocalDateTime finishedAt = LocalDateTime.now();
        record.setStatus(summary.success() ? "SUCCESS" : "ERROR");
        record.setResultPreviewJson(toJson(toResultMap(summary)));
        record.setFinishedAt(finishedAt);
        record.setDurationMs(Duration.between(startedAt, finishedAt).toMillis());
    }

    private void updateRecordOnFail(AIToolCallRecord record, ToolNotice.ResultSummary summary,
                                    LocalDateTime startedAt) {
        LocalDateTime finishedAt = LocalDateTime.now();
        record.setStatus("ERROR");
        record.setErrorMessage(summary.summary());
        record.setResultPreviewJson(toJson(toResultMap(summary)));
        record.setFinishedAt(finishedAt);
        record.setDurationMs(Duration.between(startedAt, finishedAt).toMillis());
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
}
