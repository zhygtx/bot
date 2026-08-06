package com.example.demo.ai.ai.util;

import com.example.demo.ai.ai.pojo.entity.AIChatMessage;
import com.example.demo.ai.ai.pojo.entity.AIMessageSummary;
import com.example.demo.ai.ai.pojo.entity.Code;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class AIUtil {

    @Value("${plugin.template.pom-path}")
    private  String pomTemplatePath;

    private final ObjectMapper objectMapper;

    public AIUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // ==================== 模板加载 ====================

    /**
     * 载入提示词模板文件
     */
    public String loadTemplate(String templatePath) throws IOException {
        String path = templatePath;
        if (path.startsWith("classpath:")) {
            path = path.substring("classpath:".length());
        }
        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            throw new IOException("找不到提示词模板文件: " + templatePath);
        }
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    // ==================== JSON 工具 ====================

    /** 序列化为 JSON 字符串，避免调用方直接依赖 ObjectMapper */
    public String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new RuntimeException("JSON 序列化失败", e);
        }
    }

    /** 解析 JSON 字符串为 JsonNode，避免调用方直接依赖 ObjectMapper */
    public JsonNode parseJson(String content) {
        try {
            return objectMapper.readTree(stripCodeFence(content));
        } catch (Exception e) {
            throw new RuntimeException("JSON 解析失败: " + e.getMessage() + " content=" + content, e);
        }
    }

    /**
     * 去除 AI 返回内容中的 markdown 代码块包裹（```json ... ``` 或 ``` ... ```）。
     * 仅处理首尾的 ``` 行，不影响 JSON 内部内容。
     */
    private String stripCodeFence(String content) {
        if (content == null) return "";
        String trimmed = content.trim();
        if (!trimmed.startsWith("`")) return trimmed;
        // 去除开头的 ``` 或 ```json
        String[] lines = trimmed.split("\n", -1);
        int start = 0;
        int end = lines.length - 1;
        if (lines[start].trim().matches("^```.*$")) {
            start++;
        }
        if (end >= 0 && lines[end].trim().matches("^```\\s*$")) {
            end--;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = start; i <= end; i++) {
            if (!sb.isEmpty()) sb.append("\n");
            sb.append(lines[i]);
        }
        return sb.toString().trim();
    }

    /**
     * 构建完整 Spring AI 消息列表：系统提示词 + 历史（摘要/尾部）+ 当前轮用户请求。
     *
     * @param systemTemplate 写插件系统提示词
     * @param summary        最新摘要，可为 null（null 时使用全量历史）
     * @param history        当前请求之前的全部历史消息
     * @param newMessage     当前轮新消息
     * @param codes          当前轮代码文件
     */
    public List<Message> buildMessages(String systemTemplate, AIMessageSummary summary,
                                       List<AIChatMessage> history, AIChatMessage newMessage,
                                       List<Code> codes) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemTemplate));

        if (summary == null) {
            for (AIChatMessage historyMessage : history) {
                addHistoryMessages(messages, historyMessage);
            }
        } else {
            messages.add(new SystemMessage("历史摘要（覆盖至第 " + summary.getSummaryRound() + " 轮）：\n" + summary.getSummaryContent()));
            for (AIChatMessage historyMessage : history) {
                if (historyMessage.getRound() > summary.getSummaryRound()) {
                    addHistoryMessages(messages, historyMessage);
                }
            }
        }

        messages.add(new UserMessage(buildCurrentPrompt(newMessage, codes)));
        return messages;
    }

    /** 追加一条历史轮次的用户消息与 AI 文本消息 */
    private void addHistoryMessages(List<Message> messages, AIChatMessage historyMessage) {
        messages.add(new UserMessage(historyMessage.getUserMessage()));
        messages.add(new UserMessage(extractAiText(historyMessage)));
    }

    /** 构建当前轮用户 prompt：messageId + 代码摘要 + 插件摘要 + 用户需求 */
    private String buildCurrentPrompt(AIChatMessage newMessage, List<Code> codes) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("messageId: ").append(newMessage.getId()).append("\n\n");
        String codeAbstract = buildCodeAbstract(codes);
        if (codeAbstract != null && !codeAbstract.isBlank() && !codeAbstract.equals("{\"codes\":[]}")) {
            prompt.append("当前已有代码：\n").append(codeAbstract).append("\n\n");
        }
        prompt.append("当前插件摘要：\n").append(buildPluginAbstract(newMessage)).append("\n\n");
        prompt.append("用户需求：\n").append(newMessage.getUserMessage());
        return prompt.toString();
    }

    /** 组装压缩输入：已有摘要 + 其后新增轮次；没有新增轮次时返回 null 表示无需压缩 */
    public String buildCompressionInput(List<AIChatMessage> history, AIMessageSummary previous) {
        StringBuilder input = new StringBuilder();
        if (previous != null) {
            input.append("已有历史摘要：\n").append(previous.getSummaryContent()).append("\n\n");
            boolean hasTail = false;
            for (AIChatMessage historyMessage : history) {
                if (historyMessage.getRound() > previous.getSummaryRound()) {
                    appendRound(input, historyMessage);
                    hasTail = true;
                }
            }
            if (!hasTail) {
                return null;
            }
        } else {
            for (AIChatMessage historyMessage : history) {
                appendRound(input, historyMessage);
            }
        }
        return input.toString();
    }

    /** 追加一轮对话文本到压缩输入 */
    private void appendRound(StringBuilder input, AIChatMessage historyMessage) {
        input.append("用户：").append(historyMessage.getUserMessage()).append("\n");
        String aiText = extractAiText(historyMessage);
        if (!aiText.isBlank()) {
            input.append("AI：").append(aiText).append("\n");
        }
    }

    /**
     * 从 messageParts JSON 中提取纯文本内容。
     */
    private String extractAiText(AIChatMessage message) {
        String partsJson = message.getMessageParts();
        if (partsJson == null || partsJson.isBlank()) {
            return "";
        }
        try {
            List<?> parts = objectMapper.readValue(partsJson, List.class);
            StringBuilder sb = new StringBuilder();
            for (Object part : parts) {
                if (part instanceof Map<?, ?> map && "text".equals(map.get("type"))) {
                    Object content = map.get("content");
                    if (content != null) {
                        sb.append(content);
                    }
                }
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("解析 messageParts 失败: {}", e.getMessage());
            return "";
        }
    }

    /**
     * 构建新消息。
     * @param message        用户指令文本
     * @param conversationId 会话 ID
     * @param userId         用户 ID
     * @param messages       历史消息记录
     * @return 新消息
     */
    public AIChatMessage buildNewMessage(String message, String conversationId, String userId, List<AIChatMessage> messages) throws IOException {
        AIChatMessage messageBuilder = AIChatMessage.builder()
                .id(UUID.randomUUID().toString())
                .conversationId(conversationId == null ? UUID.randomUUID().toString() : conversationId)
                .userId(userId)
                .round(conversationId == null ? 1 : messages.isEmpty() ? 1 : messages.get(messages.size() - 1).getRound() + 1)
                .userMessage(message)
                .status(conversationId == null ? AIChatMessage.Status.DRAFT : messages.isEmpty() ? AIChatMessage.Status.DRAFT : messages.get(messages.size() - 1).getStatus())
                .createTime(LocalDateTime.now())
                .pom(loadTemplate(pomTemplatePath))
                .build();

        if (!messages.isEmpty()){
            AIChatMessage last = messages.get(messages.size() - 1);
            messageBuilder.setPom(last.getPom());
            messageBuilder.setPluginId(last.getPluginId());
            messageBuilder.setPluginName(last.getPluginName());
            messageBuilder.setPluginDescription(last.getPluginDescription());
            messageBuilder.setVersion(last.getVersion());
            messageBuilder.setIsPublic(last.getIsPublic());
            messageBuilder.setChangelog(last.getChangelog());
        }
        return messageBuilder;
    }

    /**
     * 构建代码摘要。
     * @param codes 代码列表
     * @return 代码摘要 JSON 字符串
     */
    public String buildCodeAbstract(List<Code> codes) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();

        // 创建数组节点
        ArrayNode codesArray = rootNode.putArray("codes");

        for (Code code : codes == null ? List.<Code>of() : codes) {
            ObjectNode codeNode = codesArray.addObject();
            codeNode.put("id", code.getId());
            codeNode.put("path", code.getPath());
            codeNode.put("description", code.getDescription());
        }
        try {
            return mapper.writeValueAsString(rootNode);
        } catch (Exception e) {
            throw new RuntimeException("JSON 序列化失败", e);
        }
    }

    /**
     * 构建插件摘要。
     * @param aiChatMessage AI 消息
     * @return 插件摘要 JSON 字符串
     */
    public String buildPluginAbstract(AIChatMessage aiChatMessage){
        return "插件名称: " + aiChatMessage.getPluginName() + "\n" +
                "插件介绍: " + aiChatMessage.getPluginDescription() + "\n" +
                "版本号: " + aiChatMessage.getVersion() + "\n" +
                "插件更新日志: " + aiChatMessage.getChangelog() + "\n" +
                "插件推送状态: " + aiChatMessage.getStatus();
    }
}
