package com.example.demo.handler;

import com.example.demo.core.processor.MessageProcessor;
import com.example.demo.core.processor.TaskProcessor;
import com.example.demo.pojo.event.GroupEvent;
import com.example.demo.pojo.event.GroupMsg;
import com.example.demo.pojo.event.PrivateMsg;
import com.mikuac.shiro.annotation.GroupDecreaseHandler;
import com.mikuac.shiro.annotation.GroupIncreaseHandler;
import com.mikuac.shiro.annotation.GroupMessageHandler;
import com.mikuac.shiro.annotation.PrivateMessageHandler;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotMessageEventInterceptor;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.MessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.dto.event.notice.GroupDecreaseNoticeEvent;
import com.mikuac.shiro.dto.event.notice.GroupIncreaseNoticeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component("demoEventHandler")
@Shiro
public class EventHandler implements BotMessageEventInterceptor {

    private final BotCoreEvent botCoreEvent;
    private final TaskProcessor taskProcessor;
    private final MessageProcessor messageProcessor;

    public EventHandler(BotCoreEvent botCoreEvent, TaskProcessor taskProcessor, MessageProcessor messageProcessor) {
        this.botCoreEvent = botCoreEvent;
        this.taskProcessor = taskProcessor;
        this.messageProcessor = messageProcessor;
    }

    /**
     * 拦截Bot消息
     * @param bot   Bot实例，用于与服务器通信
     * @param event 消息事件对象，包含事件详细信息
     * @return true 表示处理该事件，false 表示不处理该事件
     */
    @Override
    public boolean preHandle(Bot bot, MessageEvent event) {
        Long userId = event.getUserId();
        if (botCoreEvent.getBotQqs().contains(userId)) {
            log.debug("[{}]拦截到bot消息: , 发送消息的bot: {}", bot.getSelfId(), userId);
            return false;
        }
        return true;
    }

    /**
     * 拦截Bot消息处理完成
     * @param bot   Bot实例，用于与服务器通信
     * @param event 消息事件对象，包含事件详细信息
     */
    @Override
    public void afterCompletion(Bot bot, MessageEvent event){}

    /**
     * 群消息处理
     * @param bot   Bot实例，用于与服务器通信
     * @param event 群消息事件对象，包含事件详细信息
     */
    @GroupMessageHandler
    public void groupMessage(Bot bot, GroupMessageEvent event){
        log.debug("[群消息][BOT:{}] 群号: {}, 发送者: {}, 消息内容: {}",
                bot.getSelfId(), event.getGroupId(), event.getUserId(), event.getMessage());
        GroupMsg groupMsg = messageProcessor.groupProcess(bot, event);
        taskProcessor.taskProcess(bot, groupMsg);
    }

    @PrivateMessageHandler
    public void privateMessage(Bot bot, PrivateMessageEvent event){
        log.debug("[私聊消息][BOT:{}] 发送者: {}, 消息内容: {}",
                bot.getSelfId(), event.getUserId(), event.getMessage());
        PrivateMsg privateMsg = messageProcessor.privateProcess(bot, event);
        taskProcessor.taskProcess(bot, privateMsg);
    }

    /**
     * 群成员减少事件
     * @param bot   Bot实例，用于与服务器通信
     * @param event 群成员减少事件对象，包含事件详细信息
     */
    @GroupDecreaseHandler
    public void GroupDecreaseHandler(Bot bot, GroupDecreaseNoticeEvent event){
        log.debug("[群成员减少][BOT:{}] 群号: {}, 处理人: {}, 被处理人: {}",
                bot.getSelfId(), event.getGroupId(), event.getOperatorId(), event.getUserId());
        GroupEvent groupEvent = GroupEvent.builder()
                .eventType(GroupEvent.Type.GroupDecrease)
                .botId(bot.getSelfId())
                .groupId(event.getGroupId())
                .operatorId(event.getOperatorId())
                .userId(event.getUserId())
                .build();
        taskProcessor.taskProcess(bot,groupEvent);
    }

    /**
     * 群成员增加事件
     * @param bot   Bot实例，用于与服务器通信
     * @param event 群成员增加事件对象，包含事件详细信息
     */
    @GroupIncreaseHandler
    public void GroupIncreaseHandler(Bot bot, GroupIncreaseNoticeEvent event){
        log.debug("[群成员增加][BOT:{}] 群号: {}, 处理人: {}, 被处理人: {}",
                bot.getSelfId(), event.getGroupId(), event.getOperatorId(), event.getUserId());
        GroupEvent groupEvent = GroupEvent.builder()
                .eventType(GroupEvent.Type.GroupIncrease)
                .botId(bot.getSelfId())
                .groupId(event.getGroupId())
                .operatorId(event.getOperatorId())
                .userId(event.getUserId())
                .build();
        taskProcessor.taskProcess(bot,groupEvent);
    }
}
