package com.example.demo.core.engine.executor;


import com.example.demo.pojo.msg.GroupMsg;
import com.example.demo.pojo.task.Action;
import com.example.demo.service.task.actionContent.TextService;
import com.mikuac.shiro.common.utils.MsgUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TextExecutor {

    private final TextService textService;

    @Autowired
    public TextExecutor(TextService textService) {
        this.textService = textService;
    }

    /**
     * 获取文本内容
     * @param action 动作对象
     * @param msg 群消息对象
     * @return 文本内容
     */
    public String getText(Action action, Object msg){
        log.debug("开始获取文本内容，动作ID: {}, 数据ID: {}", action.getId(), action.getDataId());
        
        // 获取文本内容
        String textContent = textService.getText(action.getDataId()).getText();
        log.debug("获取到文本内容: {}", textContent);
        
        // 构建消息
        MsgUtils builder = MsgUtils.builder()
                .text(textContent);
        log.debug("添加提取文本: {}", action.getExtractText().toString());

        if (action.getExtractText() != null){
            for (String extractText : action.getExtractText()) {
                log.debug("添加提取文本: {}", extractText);
                builder.text(extractText);
            }
        }

        // 如果需要@用户，则添加@操作
        if (action.isNeedAt()) {
            log.debug("需要@用户，用户ID: {}", ((GroupMsg)msg).getUserId());
            builder.at(((GroupMsg)msg).getUserId());
        }

        String result = builder.build();
        log.debug("文本内容构建完成，结果: {}", result);
        return result;
    }

}
