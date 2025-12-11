package com.example.demo.handler;

import com.example.demo.core.processor.MessageProcessor;
import com.example.demo.core.processor.TaskProcessor;
import com.example.demo.pojo.msg.GroupMsg;
import com.mikuac.shiro.annotation.GroupMessageHandler;
import com.mikuac.shiro.annotation.PrivateMessageHandler;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 消息事件处理器
 * 处理来自群聊和私聊的消息事件
 */
@Slf4j
@Component
@Shiro
public class MessageHandler {

    private final MessageProcessor messageProcessor;
    private final TaskProcessor taskProcessor;

    @Autowired
    public MessageHandler(MessageProcessor messageProcessor, TaskProcessor taskProcessor) {
        this.messageProcessor = messageProcessor;
        this.taskProcessor = taskProcessor;
    }

    /**
     * 处理群消息事件
     * 当机器人收到群消息时会调用此方法
     * @param bot   Bot实例，用于与服务器通信
     * @param event 群消息事件对象，包含消息详细信息
     */
    @GroupMessageHandler
    public void groupMessage(Bot bot, GroupMessageEvent event) {
        // 打印群消息到控制台
        log.info("[群消息][BOT:{}] 群号: {}, 发送者: {}, 消息内容: {}",
                bot.getSelfId(), event.getGroupId(), event.getUserId(), event.getMessage());
        if (bot.getSelfId()!=event.getUserId()){
            GroupMsg groupMsg = messageProcessor.groupProcess(bot, event);
            String result = taskProcessor.taskProcess(groupMsg);
            if (result!=null){
                bot.sendGroupMsg(event.getGroupId(), result, false);
            }
        }
    }

    /**
     * 处理私聊消息事件
     * 当机器人收到私聊消息时会调用此方法
     * @param bot   Bot实例，用于与服务器通信
     * @param event 私聊消息事件对象，包含消息详细信息
     */
    @PrivateMessageHandler
    public void privateMessage(Bot bot, PrivateMessageEvent event) {
        // 打印私聊消息到控制台
        log.info("[私聊消息][BOT:{}] 发送者: {}, 消息内容: {}",
                bot.getSelfId(), event.getUserId(), event.getMessage());
    }
}