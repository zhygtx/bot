package com.example.demo.core.engine.executor;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.example.demo.pojo.event.ChatContext;
import com.example.demo.pojo.event.GroupMsg;
import com.example.demo.pojo.event.Msg;
import com.example.demo.pojo.event.PrivateMsg;
import com.example.demo.pojo.task.Action;
import com.example.demo.pojo.task.actionContent.Ai;
import com.example.demo.service.event.ChatContextService;
import com.example.demo.utils.AiUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class AiExecutor {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChatContextService chatContextService;
    private final AiUtil aiUtil;

    private AiExecutor(ChatContextService chatContextService, AiUtil aiUtil) {
        this.chatContextService = chatContextService;
        this.aiUtil = aiUtil;
    }

    private final Ai ai = Ai.builder()
            .id("1")
            .userId("1")
            .name("AI")
            .compressPct(0.7)
            .apiKey("sk-645dacce78a0436d8611a3b598989712")
            .setting("你一个群猫娘，是群里面的吉祥物，喜欢在回复中附带颜文字来表示心情，但是不需要使用描述动作")
            .model(Ai.Model.qwen3_vl_plus_2025_12_19)
            .build();

    public String executeAi(Action action, Object objMsg){
        return msgExecute(action, objMsg);
    }

    /**
     * 群消息处理
     */
    @SneakyThrows
    private String msgExecute(Action action, Object objMsg){
        List<String> aiAbility = Ai.Model.getAbility(ai.getModel());
        //获取历史消息
        List<ChatContext> chatContexts = new ArrayList<>();
        if (objMsg instanceof GroupMsg groupMsg){
            chatContexts = chatContextService.getGroupChatContext(groupMsg.getGroupId(), groupMsg.getBotId());
        }else if (objMsg instanceof PrivateMsg privateMsg){
            chatContexts = chatContextService.getUserChatContext(privateMsg.getUserId(), privateMsg.getBotId());
        }
        Msg msg = (Msg) objMsg;

        List<MultiModalMessage> messages = new ArrayList<>();

        //1添加AI设定与历史上下文
        MultiModalMessage settingMessage = MultiModalMessage.builder()
                .role(Role.SYSTEM.getValue())
                .content(List.of(Collections.singletonMap("text", ai.getSetting())))
                .build();
        messages.add(settingMessage);
        if (!chatContexts.isEmpty() && chatContexts.get(0).getIsSummary()){
            MultiModalMessage summaryMessage = MultiModalMessage.builder()
                    .role(Role.SYSTEM.getValue())
                    .content(List.of(Collections.singletonMap("text", "历史消息总结:" + chatContexts.get(0).getMsg())))
                    .build();
            messages.add(summaryMessage);
            chatContexts.remove(0);
        }

        //2添加上下文消息(如果为空的话就没加入内容)
        for (ChatContext chatContext : chatContexts){
            MultiModalMessage userMessage = MultiModalMessage.builder()
                    .content(new ArrayList<>(List.of(Collections.singletonMap("text","用户" + chatContext.getUserId()+":"))))
                    .role(chatContext.getSenderType().equals("user") ? Role.USER.getValue() : Role.ASSISTANT.getValue()) // 根据消息来源设置角色
                    .build();

            //转换消息上下文的内容
            List<String> msgType = objectMapper.readValue(chatContext.getMsgType(), new TypeReference<>() {});
            Map<Integer, Map<String, Object>> msgContent = objectMapper.readValue(chatContext.getMsg(), new TypeReference<>() {});

            //添加消息
            for (int i = 0; i < msgType.size(); i++){
                switch (msgType.get(i)){
                    //根据模型能力判断是否添加内容，如果模型能力不包含该内容则跳过该内容
                    case "text" -> {if (aiAbility.contains("text"))    userMessage.getContent().add(Collections.singletonMap("text",msgContent.get(i).get("text")));}
                    case "image" -> {if (aiAbility.contains("image"))  userMessage.getContent().add(Collections.singletonMap("url", msgContent.get(i).get("url")));}
                }
            }

            messages.add(userMessage);
        }

        //3添加当前消息
        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(new ArrayList<>(List.of(Collections.singletonMap("text","用户" + msg.getUserId()+":" ))))
                .build();
        for (int i = 0; i < msg.getType().size(); i++){
            switch (msg.getType().get(i)){
                case "text" ->{if (aiAbility.contains("text"))     userMessage.getContent().add(Collections.singletonMap("text",msg.getContent().get(i).get("text")));}
                case "image" ->{if (aiAbility.contains("image"))   userMessage.getContent().add(Collections.singletonMap("url", msg.getContent().get(i).get("url")));}
            }
        }
        messages.add(userMessage);

        //4调用Ai
        MultiModalConversationResult result;
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


        String resultMsg = result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text").toString();
        Integer useToken = result.getUsage().getTotalTokens();

        //5将此次消息与回复内容结构化
        ChatContext userChatContext = ChatContext.builder()
                .id(UUID.randomUUID().toString())
                .botId(msg.getBotId())
                .senderType("user")
                .groupId(msg.getGroupId())
                .userId(msg.getUserId())
                .msgType(objectMapper.writeValueAsString(msg.getType()))
                .msg(objectMapper.writeValueAsString(msg.getContent()))
                .isSummary(false)
                .summaryId(chatContexts.isEmpty() ? null : chatContexts.get(chatContexts.size() - 1).getSummaryId())
                .useToken(useToken)
                .time(System.currentTimeMillis())
                .build();
        ChatContext aiChatContext = ChatContext.builder()
                .id(UUID.randomUUID().toString())
                .botId(msg.getBotId())
                .senderType("assistant")
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
            List<MultiModalMessage> summaryMessages = messages.subList(0, messages.size()/2);
            MultiModalMessage summaryMessage = MultiModalMessage.builder()
                    .role(Role.SYSTEM.getValue())
                    .content(List.of(Collections.singletonMap("text", """
                            请将我们之前的对话压缩成一个上下文摘要，要求准确精炼尽量减少输出内容,不需要语气词与颜文字之类的任何其他无关内容
                            包含：
                            1. 角色基础设定（身份、背景、性格特点）
                            2. 对话情境（时间、地点、当前状况）
                            3. 已发生的关键事件（按时间顺序）
                            4. 角色当前的心理状态和目标
                            5. 悬而未决的问题
                            输出格式：【角色设定】...【当前情境】...【关键事件】...【心理状态】...【待解决问题】...""")))
                    .build();
            summaryMessages.add(summaryMessage);
            MultiModalConversationResult summaryResult = aiUtil.call(ai, summaryMessages);
            String summaryMsg ="对话历史总结：" +summaryResult.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text").toString();
            Integer summaryUseToken = summaryResult.getUsage().getTotalTokens();
            String summaryChatContextId = UUID.randomUUID().toString();
            ChatContext summaryChatContext = ChatContext.builder()
                    .id(summaryChatContextId)
                    .botId(msg.getBotId())
                    .senderType("assistant")
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