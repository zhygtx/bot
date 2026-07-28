package com.example.demo.ai.ai.util;

import com.example.demo.ai.ai.pojo.entity.AIChatMessage;
import com.example.demo.ai.ai.pojo.entity.Code;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
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

    /**
     * 构建 Spring AI 消息列表
     */
    public List<Message> buildMessages(List<AIChatMessage> messages) {
        List<Message> springMessages = new ArrayList<>();
        for (AIChatMessage message : messages) {
            springMessages.add(new UserMessage(message.getUserMessage()));
            springMessages.add(new UserMessage(extractAiText(message)));
        }
        return springMessages;
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
