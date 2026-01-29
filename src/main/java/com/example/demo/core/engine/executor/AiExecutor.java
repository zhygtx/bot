package com.example.demo.core.engine.executor;

import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.example.demo.pojo.event.ChatContext;
import com.example.demo.pojo.event.GroupMsg;
import com.example.demo.pojo.event.Msg;
import com.example.demo.pojo.event.PrivateMsg;
import com.example.demo.pojo.task.Action;
import com.example.demo.pojo.task.actionContent.Ai;
import com.example.demo.service.event.ChatContextService;
import com.example.demo.service.task.actionContent.AiService;
import com.example.demo.utils.AiUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class AiExecutor {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChatContextService chatContextService;
    private final AiUtil aiUtil;
    private final AiService aiService;

    public AiExecutor(ChatContextService chatContextService, AiUtil aiUtil, AiService aiService) {
        this.chatContextService = chatContextService;
        this.aiUtil = aiUtil;
        this.aiService = aiService;
    }

    public String executeAi(Action action, Object objMsg){
        return msgExecute(action, objMsg);
    }

    private static final String SUMMARY_PROMPT = """
    请将以下对话历史压缩成一个精炼的上下文摘要，要求：
    1. 保留核心对话内容和关键信息
    2. 维持原始对话的角色设定和背景信息
    3. 突出重要决策点和结论
    4. 字数控制在原长度的30%以内
    5. 保持逻辑连贯性，便于后续对话理解
    
    摘要格式：
    【角色设定】[身份、背景、性格]
    【对话主题】[主要讨论内容]
    【关键进展】[重要节点和结论]
    【当前状态】[未解决事项和下一步方向]
    """;

    /**
     * 群消息处理
     */
    @SneakyThrows
    private String msgExecute(Action action, Object objMsg){
        Ai ai =aiService.getAiById(action.getDataId());

        //获取历史消息
        List<ChatContext> chatContexts = new ArrayList<>();
        if (objMsg instanceof GroupMsg groupMsg){
            chatContexts = chatContextService.getGroupChatContext(groupMsg.getGroupId(), groupMsg.getBotId());
        }else if (objMsg instanceof PrivateMsg privateMsg){
            chatContexts = chatContextService.getUserChatContext(privateMsg.getUserId(), privateMsg.getBotId());
        }
        Msg msg = (Msg) objMsg;

        List<Object> messages = new ArrayList<>();

        //1添加AI设定与历史上下文
        messages.add(aiUtil.setting(ai));
        if (!chatContexts.isEmpty() && chatContexts.get(0).getIsSummary()){
            messages.add(aiUtil.content(ai, chatContexts.get(0)));
            chatContexts.remove(0);
        }

        //2添加上下文消息(如果为空的话就没加入内容)
        for (ChatContext chatContext : chatContexts){
            Object userMessage = aiUtil.content(ai, chatContext);
            messages.add(userMessage);
        }

        //3添加当前消息
        //结构化当前消息
        ChatContext userChatContext = ChatContext.builder()
                .id(UUID.randomUUID().toString())
                .botId(msg.getBotId())
                .role("user")
                .groupId(msg.getGroupId())
                .userId(msg.getUserId())
                .msgType(objectMapper.writeValueAsString(msg.getType()))
                .msg(objectMapper.writeValueAsString(msg.getContent()))
                .isSummary(false)
                .summaryId(chatContexts.isEmpty() ? null : chatContexts.get(chatContexts.size() - 1).getSummaryId())
                .time(System.currentTimeMillis())
                .build();
        messages.add(aiUtil.content(ai, userChatContext));

        //4调用Ai
        Object result;
        try {
            result = aiUtil.call(ai, messages);
        } catch (NoApiKeyException | UploadFileException e) {
            log.warn("AI调用失败: {}", e.getMessage(), e);
            return "AI调用失败: " + e.getMessage();
        } catch (com.alibaba.dashscope.exception.ApiException e) {
            log.error("API调用异常: {}", e.getMessage(), e);
            return "AI服务异常: " + e.getMessage();
        } catch (Exception e) {  // 捕获所有其他异常
            log.error("未知异常: {}", e.getMessage(), e);
            return "AI服务异常: " + e.getMessage();
        }


        String resultMsg = aiUtil.getResultMsg(result);
        Integer useToken = aiUtil.getToken(result);

        //5将此次消息与回复内容结构化
        userChatContext.setUseToken(useToken);
        ChatContext aiChatContext = ChatContext.builder()
                .id(UUID.randomUUID().toString())
                .botId(msg.getBotId())
                .role("assistant")
                .groupId(msg.getGroupId())
                .userId(msg.getUserId())
                .msgType(objectMapper.writeValueAsString(List.of("text")))
                .msg(objectMapper.writeValueAsString(Collections.singletonMap(0,(Collections.singletonMap("text", resultMsg)))))
                .isSummary(false)
                .summaryId(chatContexts.isEmpty() ? null : chatContexts.get(chatContexts.size() - 1).getSummaryId())
                .useToken(useToken)
                .time(System.currentTimeMillis()+1)
                .build();


        //6查看是否需要并进行压缩上下文
        if (Ai.Model.getMaxToken(ai.getModel()) * ai.getCompressPct() <= useToken){
            List<Object> summaryMessages = messages.subList(0, messages.size()/2);

            //添加压缩提示
            ChatContext summaryMessage = ChatContext.builder()
                    .role("system")
                    .msgType(objectMapper.writeValueAsString(List.of("text")))
                    .msg(objectMapper.writeValueAsString(Collections.singletonMap(0,(Collections.singletonMap("text", SUMMARY_PROMPT)))))
                    .build();
            summaryMessages.add(aiUtil.content(ai, summaryMessage));

            Object summaryResult = aiUtil.call(ai, summaryMessages);
            String summaryMsg ="对话历史总结：" +aiUtil.getResultMsg(summaryResult);
            Integer summaryUseToken = aiUtil.getToken(summaryResult);

            String summaryChatContextId = UUID.randomUUID().toString();
            ChatContext summaryChatContext = ChatContext.builder()
                    .id(summaryChatContextId)
                    .botId(msg.getBotId())
                    .role("system")
                    .groupId(msg.getGroupId())
                    .userId(msg.getUserId())
                    .msgType(objectMapper.writeValueAsString(List.of("text")))
                    .msg(objectMapper.writeValueAsString(Collections.singletonMap(0,(Collections.singletonMap("text", summaryMsg)))))
                    .isSummary(true)
                    .useToken(summaryUseToken)
                    .summaryId(summaryChatContextId)
                    .time(chatContexts.get(0).getTime()-1)
                    .build();
            //设置当此对话的上下文ID
            userChatContext.setSummaryId(summaryChatContextId);
            aiChatContext.setSummaryId(summaryChatContextId);
            //更新数据库，重新绑定上下文
            chatContextService.update(chatContexts.subList(chatContexts.size()/2, chatContexts.size()), summaryChatContextId);
            chatContextService.insert(summaryChatContext);
        }

        //首次对话设置上下文
        if (chatContexts.isEmpty()){
            userChatContext.setSummaryId(userChatContext.getId());
            aiChatContext.setSummaryId(userChatContext.getId());
        }

        //7插入数据库
        chatContextService.insert(userChatContext);
        chatContextService.insert(aiChatContext);

        //8返回结果
        return resultMsg;
    }
}