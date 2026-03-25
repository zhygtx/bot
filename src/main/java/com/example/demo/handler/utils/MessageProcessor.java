package com.example.demo.handler.utils;

import com.example.demo.pojo.event.GroupMsg;
import com.example.demo.pojo.event.PrivateMsg;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.model.ArrayMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息处理类
 * 进行基础信息处理
 */
@Slf4j
@Component
public class MessageProcessor {

    /**
     * 群消息处理
     * @param bot   Bot实例，用于与服务器通信
     * @param event 群消息事件对象，包含消息详细信息
     */
    public GroupMsg groupProcess(Bot bot, GroupMessageEvent event) {
        GroupMsg groupMsg = GroupMsg.builder()
                .groupId(event.getGroupId())
                .userId(event.getUserId())
                .userRole(event.getSender().getRole())
                .botId(bot.getSelfId())
                .data(event)
                .type(new ArrayList<>())
                .content(new HashMap<>())
                .eventType(GroupMsg.Type.GroupMsg)
                .build();

        long botId = bot.getSelfId();
        boolean isAtBot = false;

        int index = 0;
        for (ArrayMsg msg : event.getArrayMsg()) {
            String type = msg.getType().toString();
            groupMsg.getType().add(type);

            Map<String, Object> dataMap = new HashMap<>();
            if (msg.getData() != null) {
                msg.getData().fieldNames().forEachRemaining(fieldName -> dataMap.put(fieldName, msg.getData().get(fieldName).asText()));
            }

            // 在同一循环中检查是否@了机器人
            if ("at".equals(type) && !isAtBot) {
                Object qqObj = dataMap.get("qq");
                if (qqObj != null && botId == Long.parseLong(qqObj.toString())) {
                    isAtBot = true;
                }
            }

            groupMsg.getContent().put(index, dataMap);
            index++;
        }

        groupMsg.setAt(isAtBot);
        return groupMsg;
    }

    /**
     * 私聊消息处理
     * @param bot   Bot实例，用于与服务器通信
     * @param event 私聊消息事件对象，包含消息详细信息
     */
    public PrivateMsg privateProcess(Bot bot, PrivateMessageEvent event) {

        PrivateMsg privateMsg = PrivateMsg.builder()
                .userId(event.getUserId())
                .botId(bot.getSelfId())
                .data(event)
                .type(new ArrayList<>())
                .content(new HashMap<>())
                .eventType(PrivateMsg.Type.PrivateMsg)
                .build();

        int index = 0;
        for (ArrayMsg msg : event.getArrayMsg()) {
            String type = msg.getType().toString();
            privateMsg.getType().add(type);

            Map<String, Object> dataMap = new HashMap<>();
            if (msg.getData() != null) {
                msg.getData().fieldNames().forEachRemaining(fieldName -> dataMap.put(fieldName, msg.getData().get(fieldName).asText()));
            }

            privateMsg.getContent().put(index, dataMap);
            index++;
        }

        return privateMsg;
    }
}
