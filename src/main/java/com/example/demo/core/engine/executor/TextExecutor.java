package com.example.demo.core.engine.executor;


import com.example.demo.pojo.event.Event;
import com.example.demo.pojo.task.Action;
import com.example.demo.service.task.actionContent.TextService;
import com.example.demo.utils.StringTemplateUtil;
import com.mikuac.shiro.common.utils.MsgUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TextExecutor {

    private final TextService textService;
    private final StringTemplateUtil stringTemplateUtil;

    public TextExecutor(TextService textService, StringTemplateUtil stringTemplateUtil) {
        this.textService = textService;
        this.stringTemplateUtil = stringTemplateUtil;
    }

    /**
     * 构建发送的文本内容
     * @param action 动作对象
     * @param msg 群消息对象
     * @return 文本内容
     */
    public String getText(Action action, Object msg){
        log.debug("开始获取文本内容，动作ID: {}, 数据ID: {}", action.getId(), action.getDataId());
        
        // 获取文本内容
        String textContent = textService.getText(action.getDataId()).getText();
        log.debug("获取到文本内容: {}", textContent);

        textContent = stringTemplateUtil.render(textContent, ((Event) msg).getData(), action.getExtractText());
        log.debug("文本内容渲染完成: {}", textContent);
        
        // 构建消息
        MsgUtils builder = MsgUtils.builder()
                .text(textContent);

        String result = builder.build();
        log.debug("文本内容构建完成，结果: {}", result);
        return result;
    }

}
