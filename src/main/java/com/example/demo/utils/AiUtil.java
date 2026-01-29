package com.example.demo.utils;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.example.demo.pojo.event.ChatContext;
import com.example.demo.pojo.task.actionContent.Ai;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AiUtil {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 调用AI接口
     * @param ai AI信息
     * @param messages 输入消息列表
     * @return 输出结果
     */
    public Object call(Ai ai, List<Object> messages) throws NoApiKeyException, InputRequiredException, UploadFileException {
        if (messages.get(0) instanceof MultiModalMessage){
            return callMultiModalConversation(ai, messages);
        }else {
            return callGeneration(ai,messages);
        }
    }

    /**
     * 调用多模对话接口
     * @param ai AI信息
     * @param messages 消息列表
     * @return 多模对话结果
     */
    private MultiModalConversationResult callMultiModalConversation(Ai ai, List<Object> messages) throws NoApiKeyException, UploadFileException {
        // 创建多模态对话实例
        MultiModalConversation conv = new MultiModalConversation();

        // 构建多模态对话参数,包括apiKey、模型名称、消息列表
        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .apiKey(ai.getApiKey())
                .model(ai.getModel().getModelName())
                .messages(messages)
                .build();

        // 配置联网搜索
        if (Ai.Model.getIsNetSearch(ai.getModel()) && ai.getNetSearch()){
            param.setEnableSearch(true);
        }

        return conv.call(param);
    }

    /**
     * 调用纯文本AI接口
     * @param ai AI信息
     * @param messages 输入消息列表
     * @return 生成结果
     */
    private GenerationResult callGeneration(Ai ai, List<Object> messages) throws NoApiKeyException, InputRequiredException {
        // 创建纯文本对话实例
        Generation generation = new Generation();

        // 构建文本对话参数,包括apiKey、模型名称、消息列表
        GenerationParam param = GenerationParam.builder()
                .apiKey(ai.getApiKey())
                .model(ai.getModel().getModelName())
                .messages(messages.stream().map(msg -> (Message) msg).toList())
                .build();

        // 配置联网搜索
        if (Ai.Model.getIsNetSearch(ai.getModel()) && ai.getNetSearch()){
            param.setEnableSearch(true);
        }

        return generation.call(param);
    }

    /**
     * 转换消息上下文内容
     * @param ai AI信息
     * @param chatContext 消息上下文
     * @return 转换后的内容
     */
    @SneakyThrows
    public Object content(Ai ai, ChatContext chatContext){
        //获取模型能力
        List<String> aiAbility = Ai.Model.getAbility(ai.getModel());
        //转换消息上下文的内容
        List<String> msgType = objectMapper.readValue(chatContext.getMsgType(), new TypeReference<>() {});
        Map<Integer, Map<String, Object>> msg = objectMapper.readValue(chatContext.getMsg(), new TypeReference<>() {});

        if (aiAbility.size() != 1 && (aiAbility.contains("image") || aiAbility.contains("video"))){
            MultiModalMessage message = MultiModalMessage.builder()
                    .role(chatContext.getRole())
                    .content(new ArrayList<>())
                    .build();
            if (chatContext.getRole().equals("user")) message.getContent().add(Collections.singletonMap("text", "用户" + chatContext.getUserId() + ":"));

            for (int i = 0; i < msgType.size(); i++){
                switch (msgType.get(i)){
                    //根据模型能力判断是否添加内容，如果模型能力不包含该内容则跳过该内容
                    case "text" -> {if (aiAbility.contains("text"))    message.getContent().add(Collections.singletonMap("text",msg.get(i).get("text")));}
                    case "image" -> {if (aiAbility.contains("image"))  message.getContent().add(Collections.singletonMap("image", msg.get(i).get("url")));}
                }
            }
            return message;
        }else {
            Message message = Message.builder()
                    .role(chatContext.getRole())
                    .content("")
                    .build();
            if (chatContext.getRole().equals("user")) message.setContent("用户" + chatContext.getUserId() + ":");
            for (int i = 0; i < msgType.size(); i++){
                if (msgType.get(i).equals("text")) message.setContent(message.getContent() + msg.get(i).get("text"));
            }
            return message;
        }
    }

    /**
     * 获取模型设置
     * @param ai AI信息
     * @return 模型设置
     */
    public Object setting(Ai ai){
        List<String> aiAbility = Ai.Model.getAbility(ai.getModel());
        if (aiAbility.size() != 1 && (aiAbility.contains("image") || aiAbility.contains("video"))){
            return MultiModalMessage.builder()
                    .role(Role.SYSTEM.getValue())
                    .content(List.of(Collections.singletonMap("text", ai.getSetting())))
                    .build();
        }else {
            return Message.builder()
                    .role(Role.SYSTEM.getValue())
                    .content(ai.getSetting())
                    .build();
        }
    }

    /**
     * 获取结果消息
     * @param result 结果
     * @return 结果消息
     */
    public String getResultMsg(Object result){
        if (result instanceof GenerationResult){
            return ((GenerationResult) result).getOutput().getText();
        }else {
            return ((MultiModalConversationResult) result).getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text").toString();
        }
    }

    /**
     * 获取结果Token
     * @param result 结果
     * @return 结果Token
     */
    public Integer getToken(Object result) {
        if (result instanceof GenerationResult){
            return ((GenerationResult) result).getUsage().getTotalTokens();
        }else {
            return ((MultiModalConversationResult) result).getUsage().getTotalTokens();
        }
    }
}
