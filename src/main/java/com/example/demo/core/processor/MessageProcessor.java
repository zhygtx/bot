package com.example.demo.core.processor;

import com.example.demo.pojo.msg.GroupMsg;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import org.springframework.stereotype.Component;

/**
 * 消息处理类
 * 进行基础信息处理
 */
@Component
public class MessageProcessor {

    /**
     * 群消息处理
     * @param bot   Bot实例，用于与服务器通信
     * @param event 群消息事件对象，包含消息详细信息
     */
    public GroupMsg groupProcess(Bot bot, GroupMessageEvent event) {
        return GroupMsg.builder()
                .groupId(event.getGroupId())
                .userId(event.getUserId())
                .userRole(event.getSender().getRole())
                .botId(bot.getSelfId())
                .type(event.getArrayMsg().stream().map(msg -> msg.getType().toString()).toList())
                .content(event.getArrayMsg().stream().map(msg -> msg.getData().toString()).toList())
                .build();
    }

}
