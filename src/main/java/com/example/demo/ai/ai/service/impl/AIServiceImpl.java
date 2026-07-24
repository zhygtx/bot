package com.example.demo.ai.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.ai.ai.client.DynamicChatClientFactory;
import com.example.demo.ai.ai.mapper.AIChatMessageMapper;
import com.example.demo.ai.ai.mapper.AIToolCallRecordMapper;
import com.example.demo.ai.ai.mapper.CodeMapper;
import com.example.demo.ai.ai.mcp.ToolCallNotifier;
import com.example.demo.ai.ai.pojo.dto.AIChatMessageDto;
import com.example.demo.ai.ai.pojo.dto.CompileCodeDto;
import com.example.demo.ai.ai.pojo.entity.AIChatMessage;
import com.example.demo.ai.ai.pojo.entity.AIToolCallRecord;
import com.example.demo.ai.ai.pojo.entity.Code;
import com.example.demo.ai.ai.service.AIService;
import com.example.demo.ai.ai.util.AIUtil;
import com.example.demo.ai.ai.util.CompileUtil;
import com.example.demo.ai.ai.mcp.AIStreamContext;
import com.example.demo.ai.ai.service.AIToolCallRecordService;
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
    private final CodeMapper codeMapper;
    private final AIToolCallRecordMapper toolCallRecordMapper;
    private final AIToolCallRecordService toolCallRecordService;
    private final ToolCallNotifier toolCallNotifier;

    public AIServiceImpl(AIChatMessageMapper aiChatMessageMapper, DynamicChatClientFactory dynamicChatClientFactory, DefaultProperties defaultProperties, DynamicChatClientFactory chatClientFactory, ObjectMapper objectMapper, CompileUtil compileUtil, PluginService pluginService, CodeMapper codeMapper, AIToolCallRecordMapper toolCallRecordMapper, AIToolCallRecordService toolCallRecordService, ToolCallNotifier toolCallNotifier) {
        this.aiChatMessageMapper = aiChatMessageMapper;
        this.dynamicChatClientFactory = dynamicChatClientFactory;
        this.defaultProperties = defaultProperties;
        this.chatClientFactory = chatClientFactory;
        this.objectMapper = objectMapper;
        this.compileUtil = compileUtil;
        this.pluginService = pluginService;
        this.codeMapper = codeMapper;
        this.toolCallRecordMapper = toolCallRecordMapper;
        this.toolCallRecordService = toolCallRecordService;
        this.toolCallNotifier = toolCallNotifier;
    }

    /**
     * 根据会话 ID 查询所有消息记录。
     * @param conversationId 会话 ID
     * @return 排序后的所有消息记录列表
     */
    @Override
    public List<AIChatMessage> findByConversationId(String conversationId) {
        return aiChatMessageMapper.selectByConversationIdWithToolCalls(conversationId);
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
     * 流式生成插件代码，流式完成后一次性持久化 AI 文本分段与工具调用记录。
     *
     * @param message        用户指令文本
     * @param conversationId 会话 ID
     * @param userId         用户 ID
     * @param onEvent        事件消费函数，(事件类型, 数据) → 推给前端
     * @return 本轮生成的 assistant 消息（含 messageParts/codes/toolCalls）
     */
    @Override
    public AIChatMessage aiGenerate(String message, String conversationId, String userId,
                                    BiConsumer<String, Object> onEvent, AtomicBoolean cancelled) throws IOException {

        // 查询历史消息记录
        List<AIChatMessage> messages = new ArrayList<>();
        List<Code> codes = new ArrayList<>();
        if (conversationId != null) {
            aiChatMessageMapper.update(new LambdaUpdateWrapper<AIChatMessage>()
                    .set(AIChatMessage::getStatus, AIChatMessage.Status.PUBLISHED_DRAFT)
                    .eq(AIChatMessage::getConversationId, conversationId)
                    .eq(AIChatMessage::getStatus, AIChatMessage.Status.PUBLISHED));
            messages = aiChatMessageMapper.selectList(new LambdaQueryWrapper<AIChatMessage>()
                    .eq(AIChatMessage::getConversationId, conversationId)
                    .orderByAsc(AIChatMessage::getRound));
            codes = codeMapper.selectList(new LambdaQueryWrapper<Code>()
                    .eq(Code::getMessageId, messages.get(messages.size() - 1).getId()));
        }
        String template = AIUtil.loadTemplate(pluginTemplatePath);
        AIChatMessage newMessage = buildNewMessage(message, conversationId, userId, messages);
        aiChatMessageMapper.insert(newMessage);

        if (codes != null) {
            for (Code code : codes) {
                code.setId(UUID.randomUUID().toString());
                code.setMessageId(newMessage.getId());
            }
            codeMapper.insert(codes);
        }
        List<Message> springMessages = AIUtil.buildMessages(messages);
        String codeAbstract = buildCodeAbstract(codes);

        // 构建用户提示词（messageId + 已有代码摘要 + 用户需求）
        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("messageId: ").append(newMessage.getId()).append("\n\n");
        if (codeAbstract != null && !codeAbstract.isBlank() && !codeAbstract.equals("{\"codes\":[]}")) {
            userPrompt.append("当前已有代码：\n").append(codeAbstract).append("\n\n");
        }
        userPrompt.append("用户需求：\n").append(message);

        // 组装完整消息列表：系统提示词 + 历史对话 + 当前用户消息
        List<Message> promptMessages = new ArrayList<>();
        promptMessages.add(new SystemMessage(template));
        promptMessages.addAll(springMessages);
        promptMessages.add(new UserMessage(userPrompt.toString()));

        // 获取 ChatClient（已注册 PluginFileTool）
        ChatClient chatClient = dynamicChatClientFactory.getPluginChatClient(userId);

        // 流式阶段：startContext 返回收集器，Reactor 线程上推 SSE + 收集事件到内存
        String assistantMessageId = newMessage.getId();
        AIStreamContext ctx = toolCallNotifier.startContext(
                newMessage.getConversationId(), assistantMessageId, newMessage.getRound(), onEvent);
        try {
            chatClient.prompt(new Prompt(promptMessages))
                    .stream()
                    .chatResponse()
                    .doOnNext(chatResponse -> {
                        if (cancelled != null && cancelled.get()) {
                            throw new RuntimeException("任务已取消");
                        }

                        var output = chatResponse.getResult().getOutput();
                        String text = output.getText();
                        if (text != null && !text.isEmpty()) {
                            toolCallNotifier.appendAssistantText(assistantMessageId, text);
                        }
                    })
                    .blockLast();
        } finally {
            toolCallNotifier.closeContext(assistantMessageId);
        }

        // 持久化阶段：从上下文读取快照，一次性写入 DB
        List<Map<String, Object>> partsSnapshot = ctx.getMessagePartsSnapshot();
        List<AIToolCallRecord> toolCallsSnapshot = ctx.getToolCallRecordsSnapshot();

        newMessage.setMessageParts(toJson(partsSnapshot));
        aiChatMessageMapper.update(new LambdaUpdateWrapper<AIChatMessage>()
                .set(AIChatMessage::getMessageParts, newMessage.getMessageParts())
                .eq(AIChatMessage::getId, newMessage.getId()));

        if (!toolCallsSnapshot.isEmpty()) {
            toolCallRecordService.saveBatch(toolCallsSnapshot);
        }
        newMessage.setToolCalls(toolCallsSnapshot);

        return newMessage;
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


    /**
     * 构建新消息。
     *
     * @param message        用户指令文本
     * @param conversationId 会话 ID
     * @param userId         用户 ID
     * @param messages       历史消息记录
     * @return 新消息
     */
    private AIChatMessage buildNewMessage(String message, String conversationId, String userId, List<AIChatMessage> messages) {
        AIChatMessage messageBuilder = AIChatMessage.builder()
                .id(UUID.randomUUID().toString())
                .conversationId(conversationId == null ? UUID.randomUUID().toString() : conversationId)
                .userId(userId)
                .round(conversationId == null ? 1 : messages.isEmpty() ? 1 : messages.get(messages.size() - 1).getRound() + 1)
                .userMessage(message)
                .status(conversationId == null ? AIChatMessage.Status.DRAFT : messages.isEmpty() ? AIChatMessage.Status.DRAFT : messages.get(messages.size() - 1).getStatus())
                .createTime(LocalDateTime.now())
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
            codeNode.put("content", code.getContent());
            codeNode.put("description", code.getDescription());
        }
        try {
            return mapper.writeValueAsString(rootNode);
        } catch (Exception e) {
            throw new RuntimeException("JSON 序列化失败", e);
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            log.warn("AI 消息分段序列化失败", e);
            return "[]";
        }
    }

    /**
     * 撤销指定轮次的对话记录，同时删除关联的代码文件和工具调用记录。
     */
    @Override
    @Transactional
    public void undo(String conversationId, Integer round) {
        AIChatMessage message = aiChatMessageMapper.selectOne(new LambdaQueryWrapper<AIChatMessage>()
                .eq(AIChatMessage::getConversationId, conversationId)
                .eq(AIChatMessage::getRound, round));
        if (message == null) return;
        codeMapper.delete(new LambdaQueryWrapper<Code>().eq(Code::getMessageId, message.getId()));
        toolCallRecordMapper.delete(new LambdaQueryWrapper<AIToolCallRecord>()
                .eq(AIToolCallRecord::getAssistantMessageId, message.getId()));
        aiChatMessageMapper.deleteById(message.getId());
    }
}
