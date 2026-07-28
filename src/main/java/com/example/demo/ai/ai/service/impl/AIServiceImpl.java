package com.example.demo.ai.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.ai.ai.client.DynamicChatClientFactory;
import com.example.demo.ai.ai.mapper.AIChatMessageMapper;
import com.example.demo.ai.ai.mapper.CodeMapper;
import com.example.demo.ai.ai.pojo.dto.AIChatMessageDto;
import com.example.demo.ai.ai.pojo.dto.CompileCodeDto;
import com.example.demo.ai.ai.pojo.entity.AIChatMessage;
import com.example.demo.ai.ai.pojo.entity.Code;
import com.example.demo.ai.ai.service.AIService;
import com.example.demo.ai.ai.util.AIUtil;
import com.example.demo.ai.ai.util.ChatStream;
import com.example.demo.ai.ai.util.CompileUtil;
import com.example.demo.config.DefaultProperties;
import com.example.demo.pojo.entity.plugin.PluginInfo;
import com.example.demo.pojo.entity.plugin.PluginVersion;
import com.example.demo.service.PluginService;
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;

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
    private final DefaultProperties defaultProperties;
    private final DynamicChatClientFactory dynamicChatClientFactory;
    private final AIChatMessageMapper aiChatMessageMapper;
    private final DynamicChatClientFactory chatClientFactory;
    private final PluginService pluginService;
    private final CodeMapper codeMapper;
    private final AIUtil aiUtil;

    public AIServiceImpl(AIChatMessageMapper aiChatMessageMapper, DynamicChatClientFactory dynamicChatClientFactory, DefaultProperties defaultProperties, DynamicChatClientFactory chatClientFactory, CompileUtil compileUtil, PluginService pluginService, CodeMapper codeMapper, AIUtil aiUtil) {
        this.aiChatMessageMapper = aiChatMessageMapper;
        this.dynamicChatClientFactory = dynamicChatClientFactory;
        this.defaultProperties = defaultProperties;
        this.chatClientFactory = chatClientFactory;
        this.compileUtil = compileUtil;
        this.pluginService = pluginService;
        this.codeMapper = codeMapper;
        this.aiUtil = aiUtil;
    }

    /**
     * 根据会话 ID 查询所有消息记录。
     * @param conversationId 会话 ID
     * @return 排序后的所有消息记录列表
     */
    @Override
    public List<AIChatMessage> findByConversationId(String conversationId) {
        return aiChatMessageMapper.selectList(new LambdaQueryWrapper<AIChatMessage>()
                .eq(AIChatMessage::getConversationId, conversationId)
                .orderByAsc(AIChatMessage::getRound));
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
     * 流式生成插件代码，流式完成后一次性持久化 AI 文本分段（含工具调用信息）。
     *
     * <p>所有 SSE 事件（delta / done / error）都通过 {@link ChatStream} 统一推送，
     * Controller 只负责创建 emitter 和 emitter.complete()。</p>
     *
     * <p>失败清理：如果生成过程中或生成开始时失败，清理预处理阶段产生的数据
     * （newMessage、复制的 codes、历史消息状态回滚），避免残留脏数据。</p>
     *
     * @param message        用户指令文本
     * @param conversationId 会话 ID
     * @param userId         用户 ID
     * @param emitter        SSE emitter，由本方法通过 ChatStream 直接消费
     */
    @Override
    public void aiGenerate(String message, String conversationId, String userId,
                           SseEmitter emitter) throws IOException {

        AIChatMessage newMessage = null;
        ChatStream stream = null;
        try {
            // ===== 预处理阶段 =====
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
            String template = aiUtil.loadTemplate(pluginTemplatePath);
            newMessage = aiUtil.buildNewMessage(message, conversationId, userId, messages);
            aiChatMessageMapper.insert(newMessage);

            if (codes != null) {
                for (Code code : codes) {
                    code.setId(UUID.randomUUID().toString());
                    code.setMessageId(newMessage.getId());
                }
                codeMapper.insert(codes);
            }

            // ===== 构建提示词 =====
            List<Message> springMessages = aiUtil.buildMessages(messages);
            String codeAbstract = aiUtil.buildCodeAbstract(codes);
            String pluginAbstract = aiUtil.buildPluginAbstract(newMessage);

            StringBuilder userPrompt = new StringBuilder();
            userPrompt.append("messageId: ").append(newMessage.getId()).append("\n\n");
            if (codeAbstract != null && !codeAbstract.isBlank() && !codeAbstract.equals("{\"codes\":[]}")) {
                userPrompt.append("当前已有代码：\n").append(codeAbstract).append("\n\n");
            }
            userPrompt.append("当前插件摘要：\n").append(pluginAbstract).append("\n\n");
            userPrompt.append("用户需求：\n").append(message);

            List<Message> promptMessages = new ArrayList<>();
            promptMessages.add(new SystemMessage(template));
            promptMessages.addAll(springMessages);
            promptMessages.add(new UserMessage(userPrompt.toString()));

            // ===== 流式生成阶段 =====
            ChatClient chatClient = dynamicChatClientFactory.getPluginChatClient(userId);
            ChatStream localStream = new ChatStream(emitter, aiUtil);
            stream = localStream;

            // 通过 Spring AI 原生 ToolContext 把 stream 传给 @Tool 方法，
            // 由 @Tool 方法在执行业务前主动推送 tool_call 事件
            Map<String, Object> toolContext = new HashMap<>();
            toolContext.put("stream", localStream);

            chatClient.prompt(new Prompt(promptMessages))
                    .toolContext(toolContext)
                    .stream()
                    .chatResponse()
                    .doOnNext(chatResponse -> {
                        // chunk 中不含工具调用信息（Spring AI 内部消费），统一交给 consume 处理 thinking/text
                        localStream.consume(chatResponse.getResult().getOutput());
                    })
                    .blockLast();

            // ===== 成功：持久化 messageParts + 推送 done =====
            newMessage.setMessageParts(stream.toJson());
            aiChatMessageMapper.update(new LambdaUpdateWrapper<AIChatMessage>()
                    .set(AIChatMessage::getMessageParts, newMessage.getMessageParts())
                    .eq(AIChatMessage::getId, newMessage.getId()));
            stream.done(newMessage);

        } catch (RuntimeException e) {
            // 失败：推送 error 事件 + 清理预处理数据
            if (stream != null) {
                stream.error(e.getMessage());
            }
            try {
                if (newMessage != null && newMessage.getId() != null) {
                    codeMapper.delete(new LambdaQueryWrapper<Code>()
                            .eq(Code::getMessageId, newMessage.getId()));
                    aiChatMessageMapper.deleteById(newMessage.getId());
                }
                if (conversationId != null) {
                    aiChatMessageMapper.update(new LambdaUpdateWrapper<AIChatMessage>()
                            .set(AIChatMessage::getStatus, AIChatMessage.Status.PUBLISHED)
                            .eq(AIChatMessage::getConversationId, conversationId)
                            .eq(AIChatMessage::getStatus, AIChatMessage.Status.PUBLISHED_DRAFT));
                }
            } catch (Exception cleanupEx) {
                log.error("清理生成数据失败", cleanupEx);
            }
            throw e;
        }
    }


    /**
     * 编译代码。
     *
     * @param compileCodeDto 编译参数-
     * @param emitter        SSE emitter，由本方法直接消费
     * @return 插件ID
     */
    @Override
    public void compileCode(CompileCodeDto compileCodeDto, SseEmitter emitter) throws Exception{

    }

    /**
     * 获取插件信息。
     * @param compileCodeDto 编译参数
     * @return 插件信息
     */
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
     * 撤销指定轮次的对话记录，同时删除关联的代码文件和工具调用记录。
     */
    @Override
    @Transactional
    public Boolean undo(String conversationId, Integer round) {
        LambdaQueryWrapper<AIChatMessage> queryWrapper = new LambdaQueryWrapper<AIChatMessage>()
                .eq(AIChatMessage::getConversationId, conversationId)
                .eq(AIChatMessage::getRound, round);
        return aiChatMessageMapper.delete(queryWrapper) > 0;
    }
}
