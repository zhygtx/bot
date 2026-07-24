package com.example.demo.ai.ai.mcp;

import com.example.demo.ai.ai.pojo.entity.AIToolCallRecord;
import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * AI 流式生成阶段的事件收集器。
 *
 * <p>线程模型：在 Reactor/Netty 线程上收集事件（文本增量、工具调用），
 * 流式完成后由业务线程读取快照一次性持久化。所有可变状态都用 synchronized 保护。</p>
 */
public class AIStreamContext {

    @Getter
    private final String conversationId;
    @Getter
    private final String assistantMessageId;
    @Getter
    private final Integer round;
    private final BiConsumer<String, Object> onEvent;

    private final List<Map<String, Object>> messageParts = new ArrayList<>();
    private final List<AIToolCallRecord> toolCallRecords = new ArrayList<>();
    private int sequence = 0;

    public AIStreamContext(String conversationId, String assistantMessageId, Integer round,
                           BiConsumer<String, Object> onEvent) {
        this.conversationId = conversationId;
        this.assistantMessageId = assistantMessageId;
        this.round = round;
        this.onEvent = onEvent;
    }

    /**
     * 推送 SSE 事件到前端（无需同步，fire-and-forget）。
     */
    public void emitEvent(String type, Object data) {
        onEvent.accept(type, data);
    }

    /**
     * 追加 AI 文本回复：分片推送 SSE + 收集到 messageParts。
     */
    public synchronized void appendText(String text) {
        if (text == null || text.isEmpty()) return;
        for (int i = 0; i < text.length(); i += 8) {
            int end = Math.min(i + 8, text.length());
            String chunk = text.substring(i, end);
            int partIndex = addTextPart(chunk);
            onEvent.accept("assistant_text_delta", Map.of(
                    "messageId", assistantMessageId,
                    "conversationId", conversationId,
                    "round", round,
                    "partIndex", partIndex,
                    "content", chunk
            ));
        }
    }

    /**
     * 添加工具调用记录到内存列表（流式完成后批量持久化）。
     */
    public synchronized void addToolCallRecord(AIToolCallRecord record) {
        toolCallRecords.add(record);
    }

    /**
     * 生成下一个工具调用序号。
     */
    public synchronized int nextSequence() {
        return ++sequence;
    }

    /**
     * 添加工具调用 part，返回 partIndex。
     */
    public synchronized int addToolPart(String toolCallId) {
        Map<String, Object> part = new LinkedHashMap<>();
        part.put("type", "tool_call");
        part.put("toolCallId", toolCallId);
        messageParts.add(part);
        return messageParts.size() - 1;
    }

    /**
     * 返回 messageParts 的快照副本，供业务线程持久化。
     */
    public synchronized List<Map<String, Object>> getMessagePartsSnapshot() {
        return new ArrayList<>(messageParts);
    }

    /**
     * 返回工具调用记录的快照副本，供业务线程批量持久化。
     */
    public synchronized List<AIToolCallRecord> getToolCallRecordsSnapshot() {
        return new ArrayList<>(toolCallRecords);
    }

    /**
     * 添加文本 part，返回 partIndex。如果最后一个 part 是 text 类型则追加到它。
     */
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
}
