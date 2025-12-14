package com.example.demo.core.engine.executor;


import com.example.demo.service.task.actionContent.TextService;
import com.mikuac.shiro.common.utils.MsgUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TextExecutor {

    private final TextService textService;

    @Autowired
    public TextExecutor(TextService textService) {
        this.textService = textService;
    }

    public String getText(String id ,String extractText){
        return MsgUtils.builder()
                .text(textService.getText(id).getText())
                .text("\n")
                .text(extractText)
                .text("\n")
                .build();
    }

}
