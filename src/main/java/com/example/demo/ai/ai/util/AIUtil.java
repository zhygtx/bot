package com.example.demo.ai.ai.util;

import com.example.demo.ai.ai.pojo.entity.AIChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class AIUtil {

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
            springMessages.add(new UserMessage(message.getAiMessage()));
        }
        return springMessages;
    }
}
