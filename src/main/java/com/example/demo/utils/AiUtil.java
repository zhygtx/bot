package com.example.demo.utils;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.example.demo.pojo.task.actionContent.Ai;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class AiUtil {

    /**
     * 调用多模对话接口
     * @param ai AI信息
     * @param messages 消息列表
     * @return 多模对话结果
     */
    public MultiModalConversationResult call(Ai ai, List<MultiModalMessage> messages) throws NoApiKeyException, UploadFileException {
        // 创建多模态对话实例
        MultiModalConversation conv = new MultiModalConversation();

        // 构建多模态对话参数,包括apiKey、模型名称、消息列表
        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .apiKey(ai.getApiKey())
                .model(ai.getModel().getModelName())
                .messages(messages)
                .build();

        return conv.call(param);
    }

}
