package com.example.demo.ai.ai.util;

import com.example.demo.ai.ai.pojo.entity.AIChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class AIUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // ==================== 模板加载 ====================

    /**
     * 载入提示词模板文件
     */
    public static String loadTemplate(String templatePath) throws IOException {
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
    public static List<Message> buildMessages(List<AIChatMessage> messages) {
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
    private static String extractAiText(AIChatMessage message) {
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
}
