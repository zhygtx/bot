package com.example.demo.ai.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.ai.ai.client.DynamicChatClientFactory;
import com.example.demo.ai.ai.mapper.AIChatMessageMapper;
import com.example.demo.ai.ai.mapper.CodeMapper;
import com.example.demo.ai.ai.mcp.PluginFileTool;
import com.example.demo.ai.ai.pojo.dto.AIChatMessageDto;
import com.example.demo.ai.ai.pojo.dto.CompileCodeDto;
import com.example.demo.ai.ai.pojo.entity.AIChatMessage;
import com.example.demo.ai.ai.pojo.entity.Code;
import com.example.demo.ai.ai.service.AIService;
import com.example.demo.ai.ai.util.AIUtil;
import com.example.demo.ai.ai.util.CompileUtil;
import com.example.demo.config.DefaultProperties;
import com.example.demo.pojo.entity.plugin.PluginInfo;
import com.example.demo.pojo.entity.plugin.PluginVersion;
import com.example.demo.service.PluginService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    private final CodeMapper codeMapper;

    public AIServiceImpl(AIChatMessageMapper aiChatMessageMapper, DynamicChatClientFactory dynamicChatClientFactory, DefaultProperties defaultProperties, DynamicChatClientFactory chatClientFactory, ObjectMapper objectMapper, CompileUtil compileUtil, PluginService pluginService, CodeMapper codeMapper) {
        this.aiChatMessageMapper = aiChatMessageMapper;
        this.dynamicChatClientFactory = dynamicChatClientFactory;
        this.defaultProperties = defaultProperties;
        this.chatClientFactory = chatClientFactory;
        this.objectMapper = objectMapper;
        this.compileUtil = compileUtil;
        this.pluginService = pluginService;
        this.codeMapper = codeMapper;
    }

    /**
     * 根据会话 ID 查询所有消息记录。
     * @param conversationId 会话 ID
     * @return 排序后的所有消息记录列表
     */
    @Override
    public List<AIChatMessage> findByConversationId(String conversationId) {
        List<AIChatMessage> aiChatMessages = aiChatMessageMapper.selectList(new LambdaQueryWrapper<AIChatMessage>()
                .eq(AIChatMessage::getConversationId, conversationId)
                .orderByAsc(AIChatMessage::getRound));
        List<Code> codes = codeMapper.selectList(new LambdaQueryWrapper<Code>()
                .eq(Code::getMessageId, aiChatMessages.getLast().getId()));
        aiChatMessages.getLast().setCodes(codes);
        return aiChatMessages;
    }

    /**
     * 根据用户 ID 分页查询所有消息记录。
     * @param userId 用户 ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 排序后的所有消息记录列表
     */
    @Override
    public List<AIChatMessageDto> findDtoList(String userId, Integer pageNum, Integer pageSize) {
        Integer offset = (pageNum - 1) * pageSize;
        return aiChatMessageMapper.selectByUserId(userId, offset, pageSize);
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
    @Transactional
    public List<AIChatMessage> aiGenerate(String message, String conversationId, String userId,
                                          BiConsumer<String, Object> onEvent, AtomicBoolean cancelled) throws IOException {

        // 0. 查询历史消息记录
        List<AIChatMessage> messages = new ArrayList<>();
        if (conversationId != null) {
            aiChatMessageMapper.update(new LambdaUpdateWrapper<AIChatMessage>()
                    .set(AIChatMessage::getStatus, AIChatMessage.Status.PUBLISHED_DRAFT)
                    .eq(AIChatMessage::getConversationId, conversationId)
                    .eq(AIChatMessage::getStatus, AIChatMessage.Status.PUBLISHED));
            messages = aiChatMessageMapper.selectList(new LambdaQueryWrapper<AIChatMessage>()
                    .eq(AIChatMessage::getConversationId, conversationId)
                    .orderByAsc(AIChatMessage::getRound));
        }
        String template = AIUtil.loadTemplate(pluginTemplatePath);
        AIChatMessage newMessage = buildNewMessage(message, conversationId, userId, messages);
        List<Message> springMessages = AIUtil.buildMessages(messages);
        String codeAbstract = buildCodeAbstract(newMessage.getCodes());

        // 1. 构建用户提示词（messageId + 已有代码摘要 + 用户需求）
        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("messageId: ").append(newMessage.getId()).append("\n\n");
        if (codeAbstract != null && !codeAbstract.isBlank() && !codeAbstract.equals("{\"codes\":[]}")) {
            userPrompt.append("当前已有代码：\n").append(codeAbstract).append("\n\n");
        }
        userPrompt.append("用户需求：\n").append(message);

        // 2. 组装完整消息列表：系统提示词 + 历史对话 + 当前用户消息
        List<Message> promptMessages = new ArrayList<>();
        promptMessages.add(new SystemMessage(template));
        promptMessages.addAll(springMessages);
        promptMessages.add(new UserMessage(userPrompt.toString()));

        // 3. 获取 ChatClient（已注册 PluginFileTool）
        ChatClient chatClient = dynamicChatClientFactory.getPluginChatClient(userId);
        StringBuilder aiResponse = new StringBuilder();

        // 4. 流式调用 + 推送到前端
        chatClient.prompt(new Prompt(promptMessages))
                .stream()
                .chatResponse()
                .doOnNext(chatResponse -> {
                    if (cancelled != null && cancelled.get()) {
                        throw new RuntimeException("任务已取消");
                    }

                    var output = chatResponse.getResult().getOutput();

                    // ── 工具调用：统一通过 PluginFileTool 构建事件数据 ──
                    var toolCalls = output.getToolCalls();
                    if (!toolCalls.isEmpty()) {
                        for (var tc : toolCalls) {
                            onEvent.accept("tool_call", PluginFileTool.buildEventData(tc, objectMapper));
                        }
                        return;
                    }

                    // ── 普通文本：打字机推送 ──
                    String text = output.getText();
                    if (text != null && !text.isEmpty()) {
                        aiResponse.append(text);
                        for (int i = 0; i < text.length(); i += 8) {
                            int end = Math.min(i + 8, text.length());
                            onEvent.accept("delta", text.substring(i, end));
                        }
                    }
                })
                .blockLast();

        String aiResponseString = aiResponse.toString();
        // 5. 持久化 AI 回复
        newMessage.setAiMessage(aiResponseString);
        aiChatMessageMapper.update(new LambdaUpdateWrapper<AIChatMessage>()
                .set(AIChatMessage::getAiMessage, aiResponseString)
                .eq(AIChatMessage::getId, newMessage.getId()));
        messages.add(newMessage);

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

        return null;
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


    private AIChatMessage buildNewMessage(String message, String conversationId, String userId, List<AIChatMessage> messages) {
        AIChatMessage messageBuilder = AIChatMessage.builder()
                .id(UUID.randomUUID().toString())
                .conversationId(conversationId == null ? UUID.randomUUID().toString() : conversationId)
                .userId(userId)
                .round(conversationId == null ? 1 : messages.getLast().getRound() + 1)
                .userMessage(message)
                .aiMessage(null)
                .status(conversationId == null ? AIChatMessage.Status.DRAFT : messages.getLast().getStatus())
                .createTime(LocalDateTime.now())
                .build();

        if (!messages.isEmpty()){
            AIChatMessage last = messages.getLast();
            messageBuilder.setPom(last.getPom());
            messageBuilder.setPluginId(last.getPluginId());
            messageBuilder.setPluginName(last.getPluginName());
            messageBuilder.setPluginDescription(last.getPluginDescription());
            messageBuilder.setVersion(last.getVersion());
            messageBuilder.setIsPublic(last.getIsPublic());
            messageBuilder.setChangelog(last.getChangelog());
            List<Code> codes = codeMapper.selectList(new LambdaQueryWrapper<Code>()
                    .eq(Code::getMessageId, last.getId()));
            for (Code code : codes) {
                code.setId(UUID.randomUUID().toString());
                code.setMessageId(messageBuilder.getId());
            }
            messageBuilder.setCodes(codes);
            codeMapper.insert(codes);
        }
        return messageBuilder;
    }

    public String buildCodeAbstract(List<Code> codes) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();

        // 创建数组节点
        ArrayNode codesArray = rootNode.putArray("codes");

        for (Code code : codes) {
            ObjectNode codeNode = codesArray.addObject();
            codeNode.put("id", code.getId());
            codeNode.put("path", code.getPath());
            codeNode.put("content", code.getContent());
            codeNode.put("description", code.getDescription());
        }
        try {
            return mapper.writeValueAsString(rootNode);
        } catch (Exception e) {
            throw new RuntimeException("JSON 序列化失败", e);
        }
    }
}
