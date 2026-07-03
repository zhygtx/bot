package com.example.demo.ai.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.ai.ai.client.DynamicChatClientFactory;
import com.example.demo.ai.ai.mapper.AIChatMessageMapper;
import com.example.demo.ai.ai.pojo.dto.AIChatMessageDto;
import com.example.demo.ai.ai.pojo.entity.AIChatMessage;
import com.example.demo.ai.ai.service.AIService;
import com.example.demo.ai.ai.util.AIUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

@Slf4j
@Service
public class AIServiceImpl extends ServiceImpl<AIChatMessageMapper, AIChatMessage> implements AIService {

    @Value("${ai.default.plugin.prompt-template-path}")
    private String pluginTemplatePath;

    private final DynamicChatClientFactory dynamicChatClientFactory;
    private final AIChatMessageMapper aiChatMessageMapper;

    public AIServiceImpl(AIChatMessageMapper aiChatMessageMapper, DynamicChatClientFactory dynamicChatClientFactory) {
        this.aiChatMessageMapper = aiChatMessageMapper;
        this.dynamicChatClientFactory = dynamicChatClientFactory;
    }

    /**
     * 根据会话 ID 查询所有消息记录。
     *
     * @param conversationId 会话 ID
     * @return 排序后的所有消息记录列表
     */
    @Override
    public List<AIChatMessage> findByConversationId(String conversationId) {

        return aiChatMessageMapper.selectList(new LambdaQueryWrapper<AIChatMessage>()
                .eq(AIChatMessage::getConversationId, conversationId)
                .orderByAsc(AIChatMessage::getRound)
                .orderByDesc(AIChatMessage::getRole));
    }

    @Override
    public List<AIChatMessageDto> findDtoList(String userId, Integer pageNum, Integer pageSize) {
        Integer offset = (pageNum - 1) * pageSize;
        return aiChatMessageMapper.selectByUserId(userId, offset, pageSize);
    }

    /**
     * 撤销指定轮次的对话记录。
     *
     * @param conversationId 会话 ID
     * @param round          轮次
     * @return 操作结果
     */
    @Override
    public Integer undoToRound(String conversationId, Integer round) {
        return aiChatMessageMapper.delete(new LambdaQueryWrapper<AIChatMessage>()
                .eq(AIChatMessage::getConversationId, conversationId)
                .eq(AIChatMessage::getRound, round));
    }

    /**
     * 启动流式 AI 生成任务。
     * 在 AI 流式输出过程中同步完成：
     * 1. 8 字分块推送打字机效果（事件类型 "delta"）
     * 2. 实时检测文件边界并通知前端（事件类型 "file_start" / "file_end"）
     * 3. 增量构建 code JSON（依赖、插件信息、文件列表）
     *
     * @param message        用户指令文本
     * @param conversationId 会话 ID
     * @param userId         用户 ID
     * @param onEvent        事件消费函数，(事件类型, 数据) → 推给前端
     * @return 包含 user + assistant 的完整消息列表
     */
    @Override
    public List<AIChatMessage> aiGenerate(String message, String conversationId, String userId,
                                          BiConsumer<String, Object> onEvent, AtomicBoolean cancelled) throws IOException {

        onEvent.accept("message_change", "正在获取上下文信息");

        List<AIChatMessage> messages = new ArrayList<>();
        if (!conversationId.isEmpty()) {
            messages = findByConversationId(conversationId);
        }

        AIChatMessage userMessage = AIChatMessage.builder()
                .id(UUID.randomUUID().toString())
                .conversationId(conversationId.isEmpty() ? UUID.randomUUID().toString() : conversationId)
                .pluginId(messages.isEmpty() ? null : messages.get(0).getPluginId())
                .userId(userId)
                .round(messages.isEmpty() ? 1 : messages.get(0).getRound() + 1)
                .role("user")
                .message(message)
                .status(AIChatMessage.Status.DRAFT)
                .createTime(LocalDateTime.now())
                .build();
        messages.add(userMessage);

        List<Message> springMessages = buildMessages(messages);

        // ==================== 流式调用 + 增量解析（委托给 AIUtil） ====================
        ChatClient chatClient = dynamicChatClientFactory.getPluginChatClient(userId);
        Map<String, String> aiResult = AIUtil.streamAndParse(chatClient, springMessages, onEvent, cancelled);
        String rawText = aiResult.get("rawText");
        String codeJson = aiResult.get("codeJson");

        // ==================== 持久化 ====================
        AIChatMessage aiMessage = AIChatMessage.builder()
                .id(UUID.randomUUID().toString())
                .conversationId(userMessage.getConversationId())
                .pluginId(userMessage.getPluginId())
                .userId(userMessage.getUserId())
                .round(userMessage.getRound())
                .role("assistant")
                .message(rawText)
                .status(userMessage.getStatus())
                .code(codeJson)
                .createTime(LocalDateTime.now())
                .build();
        messages.add(aiMessage);
        aiChatMessageMapper.insert(userMessage);
        aiChatMessageMapper.insert(aiMessage);

        onEvent.accept("message_change", "代码生成完毕");
        return messages;
    }


    /**
     * 构建 Spring AI 消息列表。
     * @param messages 会话消息记录
     * @return Spring AI 消息列表
     */
    private List<Message> buildMessages(List<AIChatMessage> messages) throws IOException {
        List<Message> springMessages = new ArrayList<>();

        String template;
        template = AIUtil.loadTemplate(pluginTemplatePath);

        springMessages.add(new SystemMessage(template));
        for (AIChatMessage message : messages) {
            if ("user".equals(message.getRole())){
                springMessages.add(new UserMessage(message.getMessage()));
            } else if ("assistant".equals(message.getRole())){
                springMessages.add(new AssistantMessage(message.getMessage()));
            }
        }
        return springMessages;
    }
}
