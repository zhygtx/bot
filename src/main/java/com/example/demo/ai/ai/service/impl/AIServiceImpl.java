package com.example.demo.ai.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.ai.ai.client.DynamicChatClientFactory;
import com.example.demo.ai.ai.mapper.AIChatMessageMapper;
import com.example.demo.ai.ai.pojo.dto.AIChatMessageDto;
import com.example.demo.ai.ai.pojo.dto.CompileCodeDto;
import com.example.demo.ai.ai.pojo.entity.AIChatMessage;
import com.example.demo.ai.ai.service.AIService;
import com.example.demo.ai.ai.util.AIUtil;
import com.example.demo.ai.ai.util.CompileUtil;
import com.example.demo.config.DefaultProperties;
import com.example.demo.pojo.entity.Result;
import com.example.demo.pojo.entity.plugin.PluginInfo;
import com.example.demo.pojo.entity.plugin.PluginVersion;
import com.example.demo.service.PluginService;
import com.example.demo.util.PathMultipartFile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
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

    @Value("${plugin.compiler.work-dir}")
    private String workDir;
    @Value("${ai.default.plugin.prompt-template-path}")
    private String pluginTemplatePath;
    @Value("${ai.default.review.prompt-template-path}")
    private String reviewPromptTemplatePath;

    private final CompileUtil compileUtil;
    private final ObjectMapper objectMapper;
    private final DefaultProperties defaultProperties;
    private final DynamicChatClientFactory dynamicChatClientFactory;
    private final AIChatMessageMapper aiChatMessageMapper;
    private final DynamicChatClientFactory chatClientFactory;
    private final PluginService pluginService;

    public AIServiceImpl(AIChatMessageMapper aiChatMessageMapper, DynamicChatClientFactory dynamicChatClientFactory, DefaultProperties defaultProperties, DynamicChatClientFactory chatClientFactory, ObjectMapper objectMapper, CompileUtil compileUtil, PluginService pluginService) {
        this.aiChatMessageMapper = aiChatMessageMapper;
        this.dynamicChatClientFactory = dynamicChatClientFactory;
        this.defaultProperties = defaultProperties;
        this.chatClientFactory = chatClientFactory;
        this.objectMapper = objectMapper;
        this.compileUtil = compileUtil;
        this.pluginService = pluginService;
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
        if (messages.get(0).getStatus() == AIChatMessage.Status.PUBLISHED){
            LambdaUpdateWrapper<AIChatMessage> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(AIChatMessage::getConversationId, userMessage.getConversationId())
                    .set(AIChatMessage::getStatus, AIChatMessage.Status.PUBLISHED_DRAFT);
            aiChatMessageMapper.update(null, updateWrapper);
        }

        onEvent.accept("message_change", "代码生成完毕");
        return messages;
    }

    /**
     * 编译代码。
     *
     * @param compileCodeDto 编译参数-
     * @param onEvent        事件消费函数，第一个参数为事件类型，第二个参数为事件数据
     * @return 插件ID
     */
    @Override
    public String compileCode(CompileCodeDto compileCodeDto, BiConsumer<String, Object> onEvent) throws Exception{
        AIChatMessage lastMessage = aiChatMessageMapper.selectLastCode(compileCodeDto.getConversationId());

        if (lastMessage == null) {
            throw new RuntimeException("未找到该会话的代码记录");
        }

        if (defaultProperties.getReview().getEnabled()){
            onEvent.accept("compile_change","正在使用AI审核代码");
            String reviewPrompt = AIUtil.loadTemplate(reviewPromptTemplatePath);
            ChatClient chatClient = chatClientFactory.getReviewChatClient();
            String content = chatClient.prompt()
                    .system(reviewPrompt)
                    .user(lastMessage.getCode())
                    .call()
                    .content();
            JsonNode codeJson = objectMapper.readTree(content);
            if (!codeJson.has("passed")){
                throw new RuntimeException(codeJson.get("issues").asText());
            }
        }

        onEvent.accept("compile_change","正在创建代码文件");
        compileUtil.createCodeFile(lastMessage);

        onEvent.accept("compile_change","正在编译代码");
        Path path = compileUtil.compileCode(lastMessage);

        onEvent.accept("compile_change","正在上传插件");
        compileCodeDto.setPluginId(lastMessage.getPluginId());
        PluginInfo pluginInfo = getPluginInfo(compileCodeDto);

        // 3. 转 MultipartFile + 调用上传
        MultipartFile jarFile = new PathMultipartFile(path);
        Result<?> result = pluginService.add(pluginInfo, jarFile);

        if (!Integer.valueOf(200).equals(result.getCode())) {
            throw new RuntimeException("插件上传失败: " + result.getMessage());
        }

        onEvent.accept("compile_change", "正在清理临时文件");
        compileUtil.deleteDirectoryRecursively(Path.of(workDir, lastMessage.getConversationId()));

        onEvent.accept("compile_change", "正常合并需求");
        List<AIChatMessage> messages = findByConversationId(compileCodeDto.getConversationId());

        Integer round = 0;
        StringBuilder userMsg = new StringBuilder().append("用户指令历史需求：" + "\n");
        for (AIChatMessage message : messages) {
            if ("user".equals(message.getRole())){
                round++;
                userMsg.append("第").append(round).append("轮：").append(message.getMessage()).append("\n");
            }
        }
        userMsg.append("当前插件代码：").append("\n").append(lastMessage.getCode());
        aiChatMessageMapper.deleteByConversationIdAndRound(compileCodeDto.getConversationId(), round);
        aiChatMessageMapper.updateStatusAndRound(compileCodeDto.getConversationId(), pluginInfo.getId(), AIChatMessage.Status.PUBLISHED);
        aiChatMessageMapper.updateMessage(lastMessage.getId(), userMsg.toString());
        return pluginInfo.getId();
    }

    private @NotNull PluginInfo getPluginInfo(CompileCodeDto compileCodeDto) {
        PluginInfo pluginInfo = new PluginInfo();

        // 新建 vs 更新：有 pluginId → 更新已有插件；无 pluginId → 新建
        if (compileCodeDto.getPluginId() != null && !compileCodeDto.getPluginId().isBlank()) {
            pluginInfo.setId(compileCodeDto.getPluginId());
        }
        pluginInfo.setName(compileCodeDto.getPluginName());
        pluginInfo.setDescription(compileCodeDto.getPluginDescription());
        pluginInfo.setAuthorId(compileCodeDto.getUserId());
        pluginInfo.setIsPublic(compileCodeDto.getIsPublic() != null ? compileCodeDto.getIsPublic() : false);

        // 2. 组装 PluginVersion
        PluginVersion pluginVersion = new PluginVersion();
        pluginVersion.setVersion(compileCodeDto.getPluginVersion());
        pluginVersion.setChangelog(compileCodeDto.getPluginChangeDescription());
        pluginVersion.setEntityPackage(compileCodeDto.getEntityPackage());
        pluginVersion.setMethodPackage(compileCodeDto.getMethodPackage());
        pluginInfo.setPluginVersionList(List.of(pluginVersion));
        return pluginInfo;
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
