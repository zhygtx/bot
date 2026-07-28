package com.example.demo.ai.ai.util;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SSE 流式推送的统一总线：所有向前端推送的事件（思考 / 正文 / 工具调用 / done / error）
 * 全部收口到这里，调用方不再直接接触 emitter 或 aiUtil.sendSseEvent。
 *
 * <p>同时承担 messageParts 的内存收集（text/thinking/tool_call），
 * 用一把锁（本实例）保证「读末尾 + 追加 + 推送」的原子性。</p>
 *
 * <p>用法：</p>
 * <pre>
 * ChatStream stream = new ChatStream(emitter, aiUtil);
 * stream.consume(output);                              // 自动识别 thinking/text 并推送
 * Map<String, Object> part = stream.toolCallStart("保存代码文件");  // 推送 running
 * stream.toolCallFinish(part, "保存代码文件", "SUCCESS");          // 推送 success/error
 * stream.done(message);                                // 完成
 * stream.error("失败原因");                              // 异常
 * String json = stream.toJson();                        // 持久化
 * </pre>
 */
public class ChatStream {

    private final SseEmitter emitter;
    private final AIUtil aiUtil;
    private final List<Map<String, Object>> parts = new ArrayList<>();

    public ChatStream(SseEmitter emitter, AIUtil aiUtil) {
        this.emitter = emitter;
        this.aiUtil = aiUtil;
    }

    // ===== 增量 delta =====

    /**
     * 消费一个流式 chunk：自动识别 thinking/text 并推送对应 delta 事件。
     * 统一 OpenAI/Anthropic 两种协议：
     * <ul>
     *   <li>OpenAI/DeepSeek: reasoningContent 放在 metadata，getText() 只返回正文</li>
     *   <li>Anthropic: thinking 内容直接放在 getText()，metadata 含 signature 标记</li>
     * </ul>
     */
    public synchronized void consume(AssistantMessage output) {
        Map<String, Object> metadata = output.getMetadata();
        String text = output.getText();
        String thinking = null;

        Object reasoning = metadata.get("reasoningContent");
        if (reasoning == null) {
            reasoning = metadata.get("reasoning_content");
        }
        if (reasoning != null) {
            thinking = reasoning.toString();
        } else if (metadata.containsKey("signature") && text != null && !text.isEmpty()) {
            // Anthropic thinking chunk：getText() 即思考内容
            thinking = text;
            text = null;
        }

        if (thinking != null && !thinking.isEmpty()) {
            appendTextPart("thinking", thinking);
            send("delta", Map.of("type", "thinking", "content", thinking));
        }
        if (text != null && !text.isEmpty()) {
            appendTextPart("text", text);
            send("delta", Map.of("type", "text", "content", text));
        }
    }

    /**
     * 工具调用开始：追加 RUNNING 状态的 tool_call part 并推送 delta 事件。
     * @return part 引用，调用方需在工具执行后调用 {@link #toolCallFinish} 更新状态
     */
    public synchronized Map<String, Object> toolCallStart(String name) {
        Map<String, Object> part = new LinkedHashMap<>();
        part.put("type", "tool_call");
        part.put("name", name);
        part.put("status", "RUNNING");
        parts.add(part);
        send("delta", Map.of("type", "tool_call", "name", name, "status", "RUNNING"));
        return part;
    }

    /**
     * 工具调用结束：更新 part 状态并推送 delta 事件。
     * @param status {@code "SUCCESS"} 或 {@code "ERROR"}
     */
    public synchronized void toolCallFinish(Map<String, Object> part, String name, String status) {
        part.put("status", status);
        send("delta", Map.of("type", "tool_call", "name", name, "status", status));
    }

    // ===== 终止事件 =====

    /** 推送 done 事件，data 通常是最终消息对象 */
    public void done(Object data) {
        send("done", data);
    }

    /** 推送 error 事件，message 为空时使用默认文案 */
    public void error(String message) {
        send("error", Map.of("message", message == null ? "AI 生成失败" : message));
    }

    // ===== 持久化 =====

    /** 把已收集的 parts 序列化为 JSON 字符串，供持久化使用 */
    public synchronized String toJson() {
        return aiUtil.toJson(parts);
    }

    // ===== 内部 =====

    private void appendTextPart(String type, String content) {
        if (!parts.isEmpty() && type.equals(parts.get(parts.size() - 1).get("type"))) {
            Map<String, Object> last = parts.get(parts.size() - 1);
            last.put("content", last.getOrDefault("content", "") + content);
        } else {
            Map<String, Object> part = new LinkedHashMap<>();
            part.put("type", type);
            part.put("content", content);
            parts.add(part);
        }
    }

    private void send(String event, Object data) {
        aiUtil.sendSseEvent(emitter, event, data);
    }
}
