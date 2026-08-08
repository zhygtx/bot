package com.generalbot.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.generalbot.ai.client.DynamicChatClientFactory;
import com.generalbot.ai.stream.SseStreamFactory;
import com.generalbot.ai.mapper.AIChatMessageMapper;
import com.generalbot.ai.mapper.CodeMapper;
import com.generalbot.ai.dto.AIPluginListDto;
import com.generalbot.ai.entity.AIChatMessage;
import com.generalbot.ai.entity.Code;
import com.generalbot.ai.service.AIService;
import com.generalbot.ai.service.ContextCompressionService;
import com.generalbot.ai.util.AIUtil;
import com.generalbot.ai.util.CompileUtil;
import com.generalbot.ai.stream.SseStream;
import com.generalbot.config.DefaultProperties;
import com.generalbot.plugin.entity.PluginInfo;
import com.generalbot.plugin.service.PluginService;
import com.generalbot.common.util.PathMultipartFile;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Slf4j
@Service
public class AIServiceImpl extends ServiceImpl<AIChatMessageMapper, AIChatMessage> implements AIService {

    private final CompileUtil compileUtil;
    private final DefaultProperties defaultProperties;
    private final DynamicChatClientFactory dynamicChatClientFactory;
    private final AIChatMessageMapper aiChatMessageMapper;
    private final PluginService pluginService;
    private final CodeMapper codeMapper;
    private final AIUtil aiUtil;
    private final SseStreamFactory sseStreamFactory;
    private final ContextCompressionService contextCompressionService;

    public AIServiceImpl(AIChatMessageMapper aiChatMessageMapper, DynamicChatClientFactory dynamicChatClientFactory, DefaultProperties defaultProperties, CompileUtil compileUtil, PluginService pluginService, CodeMapper codeMapper, AIUtil aiUtil, SseStreamFactory sseStreamFactory, ContextCompressionService contextCompressionService) {
        this.aiChatMessageMapper = aiChatMessageMapper;
        this.dynamicChatClientFactory = dynamicChatClientFactory;
        this.defaultProperties = defaultProperties;
        this.compileUtil = compileUtil;
        this.pluginService = pluginService;
        this.codeMapper = codeMapper;
        this.aiUtil = aiUtil;
        this.sseStreamFactory = sseStreamFactory;
        this.contextCompressionService = contextCompressionService;
    }

    /** 分页查询会话消息：初始加载最新一页，beforeRound 向上翻页 */
    @Override
    public Map<String, Object> findPageByConversationId(String conversationId, Integer beforeRound, Integer pageSize) {
        int limit = pageSize == null || pageSize <= 0 ? 30 : Math.min(pageSize, 100);
        LambdaQueryWrapper<AIChatMessage> wrapper = new LambdaQueryWrapper<AIChatMessage>()
                .eq(AIChatMessage::getConversationId, conversationId);
        if (beforeRound != null) {
            wrapper.lt(AIChatMessage::getRound, beforeRound);
        }
        PageHelper.startPage(1, limit);
        List<AIChatMessage> messages = aiChatMessageMapper.selectList(wrapper.orderByDesc(AIChatMessage::getRound));
        PageInfo<AIChatMessage> pageInfo = new PageInfo<>(messages);
        Collections.reverse(messages);
        return Map.of("messages", messages, "hasMore", pageInfo.isHasNextPage());
    }

    /**
     * 根据用户 ID 分页查询所有消息记录。
     * @param userId 用户 ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 排序后的所有消息记录列表
     */
    @Override
    public List<AIPluginListDto> findPluginList(String userId, Integer pageNum, Integer pageSize) {
        Integer offset = (pageNum - 1) * pageSize;
        return aiChatMessageMapper.selectPluginListByUserId(userId, offset, pageSize);
    }

    /**
     * 启动流式 AI 生成任务。
     * 流式生成插件代码，流式完成后一次性持久化 AI 文本分段（含工具调用信息）。
     *
     * <p>所有 SSE 事件（delta / done / error）都通过 {@link SseStream} 统一推送，
     * Controller 只负责创建 emitter 和 emitter.complete()。</p>
     *
     * <p>失败清理：如果生成过程中或生成开始时失败，清理预处理阶段产生的数据
     * （newMessage、复制的 codes、历史消息状态回滚），避免残留脏数据。</p>
     *
     * @param message        用户指令文本
     * @param conversationId 会话 ID
     * @param userId         用户 ID
     * @param emitter        SSE emitter，由本方法通过 SseStream 直接消费
     */
    @Override
    public void aiGenerate(String message, String conversationId, String userId,
                           SseEmitter emitter) throws IOException {

        AIChatMessage newMessage = null;
        SseStream stream = null;
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
            String template = aiUtil.loadTemplate(defaultProperties.getPlugin().getPromptTemplatePath());
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
            List<Message> promptMessages = contextCompressionService.buildMessages(userId, conversationId, messages, newMessage, codes, template);

            // ===== 流式生成阶段 =====
            ChatClient chatClient = dynamicChatClientFactory.getPluginChatClient(userId);
            SseStream localStream = sseStreamFactory.create(emitter);
            stream = localStream;

            // 通过 Spring AI 原生 ToolContext 把 stream 传给 @Tool 方法，
            // 由 @Tool 方法在执行业务前主动推送 tool_call 事件
            Map<String, Object> toolContext = new HashMap<>();
            toolContext.put("stream", localStream);

            ChatResponse lastResponse = chatClient.prompt(new Prompt(promptMessages))
                    .toolContext(toolContext)
                    .stream()
                    .chatResponse()
                    .doOnNext(chatResponse -> {
                        // chunk 中不含工具调用信息（Spring AI 内部消费），统一交给 consume 处理 thinking/text
                        localStream.consume(chatResponse.getResult().getOutput());
                    })
                    .blockLast();

            // ===== 成功：持久化 messageParts + 推送 done =====
            // 记录本次请求的真实 prompt token 用量，供下次生成前判断是否需要压缩
            Usage usage = lastResponse == null ? null : lastResponse.getMetadata().getUsage();
            newMessage.setPromptTokens(usage == null ? null : usage.getPromptTokens());
            newMessage.setMessageParts(stream.toJson());
            aiChatMessageMapper.update(new LambdaUpdateWrapper<AIChatMessage>()
                    .set(AIChatMessage::getMessageParts, newMessage.getMessageParts())
                    .set(AIChatMessage::getPromptTokens, newMessage.getPromptTokens())
                    .eq(AIChatMessage::getId, newMessage.getId()));
            // 工具调用过程中 AI 可能已通过 updatePluginDescription 写入插件元信息，
            // 重新从数据库读取最新消息，确保 done 事件里带回发布设置所需内容。
            AIChatMessage doneMessage = aiChatMessageMapper.selectById(newMessage.getId());
            if (doneMessage == null) {
                throw new RuntimeException("AI 消息保存后查询失败: " + newMessage.getId());
            }
            stream.done(doneMessage);

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
     * @param messageId       会话消息 ID
     * @param emitter        SSE emitter，由本方法直接消费
     */
    @Override
    public void compileCode(String messageId, SseEmitter emitter) throws Exception {
        SseStream stream = sseStreamFactory.create(emitter);
        PluginInfo pluginInfo;
        try {
            AIChatMessage message = aiChatMessageMapper.selectById(messageId);
            List<Code> codes = codeMapper.selectList(new LambdaQueryWrapper<Code>()
                    .eq(Code::getMessageId, messageId));
            if (defaultProperties.getReview().getEnabled()){
                stream.send("compile", "正在使用AI审核代码");
                ChatClient chatClient = dynamicChatClientFactory.getReviewChatClient();
                String reviewPrompt = aiUtil.loadTemplate(defaultProperties.getReview().getPromptTemplatePath());
                List<Message> promptMessages = new ArrayList<>();
                promptMessages.add(new SystemMessage(reviewPrompt));
                promptMessages.add(new UserMessage(aiUtil.toJson(codes)));

                String s = chatClient.prompt(new Prompt(promptMessages))
                        .call()
                        .content();
                JsonNode jsonNode = aiUtil.parseJson(s);
                if (!jsonNode.path("passed").asBoolean()) {
                    // review 未通过是业务分支，不是异常，单独推送 review_failed 事件
                    // 直接传 issues 节点（SseStream 会序列化为 JSON），前端按结构化展示
                    stream.send("review_failed", jsonNode.path("issues"));
                    return;
                }
            }

            stream.send("compile", "正在将代码写入文件");
            compileUtil.createCodeFile(codes, message);

            stream.send("compile", "正在编译代码");
            Path jarPath = compileUtil.compileCode(message.getConversationId());

            stream.send("compile", "正在上传插件");
            pluginInfo = compileUtil.getPluginInfo(message);
            PathMultipartFile file = new PathMultipartFile(jarPath);
            PluginInfo added = pluginService.add(pluginInfo, file);
            LambdaUpdateWrapper<AIChatMessage> updateWrapper = new LambdaUpdateWrapper<AIChatMessage>()
                    .eq(AIChatMessage::getConversationId, message.getConversationId())
                    .set(AIChatMessage::getStatus, AIChatMessage.Status.PUBLISHED)
                    .set(AIChatMessage::getPluginId, added.getId());
            aiChatMessageMapper.update(null, updateWrapper);

        } catch (Exception e) {
            // try-catch 保护，避免 SSE 发送失败掩盖原始异常
            try {
                stream.error(e.getMessage());
            } catch (Exception sendEx) {
                log.error("SSE 推送 error 事件失败", sendEx);
            }
            throw e;
        }

        stream.done(pluginInfo);
    }

    /**
     * 撤销指定轮次的对话记录，同时删除关联的代码文件和工具调用记录。
     * 只允许撤销当前最大轮次，保证摘要外键级联与"只撤最后一轮"的约束一致。
     */
    @Override
    @Transactional
    public Boolean undo(String conversationId, Integer round) {
        if (conversationId == null || round == null) {
            return false;
        }
        Integer maxRound = aiChatMessageMapper.selectList(new LambdaQueryWrapper<AIChatMessage>()
                        .eq(AIChatMessage::getConversationId, conversationId))
                .stream()
                .map(AIChatMessage::getRound)
                .max(Integer::compareTo)
                .orElse(null);
        if (maxRound == null || !maxRound.equals(round)) {
            return false;
        }
        LambdaQueryWrapper<AIChatMessage> queryWrapper = new LambdaQueryWrapper<AIChatMessage>()
                .eq(AIChatMessage::getConversationId, conversationId)
                .eq(AIChatMessage::getRound, round);
        return aiChatMessageMapper.delete(queryWrapper) > 0;
    }
}
