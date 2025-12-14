package com.example.demo.core.engine.executor;


import com.example.demo.pojo.msg.GroupMsg;
import com.example.demo.pojo.task.Action;
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

    /**
     * 获取文本内容
     * @param action 动作对象
     * @param groupMsg 群消息对象
     * @return 文本内容
     */
    public String getText(Action action, GroupMsg groupMsg){
        MsgUtils builder = MsgUtils.builder()
                .text(textService.getText(action.getDataId()).getText())
                .text("\n")
                .text(action.getExtractText())
                .text("\n");

        // 如果需要@用户，则添加@操作
        if (action.isNeedAt()) {
            builder.at(groupMsg.getUserId());
        }

        return builder.build();
    }

}
