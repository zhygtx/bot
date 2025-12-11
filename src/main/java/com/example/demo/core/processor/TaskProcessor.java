package com.example.demo.core.processor;

import com.example.demo.pojo.msg.GroupMsg;
import com.mikuac.shiro.common.utils.MsgUtils;
import org.springframework.stereotype.Component;

/**
 * 任务处理类,负责调度各模块的处理逻辑
 */
@Component
public class TaskProcessor {

    public String taskProcess(GroupMsg groupMsg) {
        return MsgUtils.builder()
                .text("群号"+groupMsg.getGroupId()+"\n")
                .text("用户"+groupMsg.getUserId()+"\n")
                .text("用户权限"+groupMsg.getUserRole()+"\n")
                .text("BotID"+groupMsg.getBotId()+"\n")
                .text("消息类型"+groupMsg.getType()+"\n")
                .text("消息内容"+groupMsg.getContent())
                .build();
    }
}