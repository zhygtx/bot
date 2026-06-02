package com.example.demo.handler;

import com.example.demo.annotation.BotEvent;
import com.example.demo.annotation.EventParam;
import com.mikuac.shiro.annotation.*;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotMessageEventInterceptor;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.MessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.dto.event.notice.GroupDecreaseNoticeEvent;
import com.mikuac.shiro.dto.event.notice.GroupIncreaseNoticeEvent;
import com.mikuac.shiro.dto.event.request.GroupAddRequestEvent;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("demoEventHandler")
@Shiro
public class EventHandler implements BotMessageEventInterceptor {

    private final BotCoreEvent botCoreEvent;
    private final BotWorkflowHandler botWorkflowHandler;

    public EventHandler(BotCoreEvent botCoreEvent, BotWorkflowHandler botWorkflowHandler) {
        this.botCoreEvent = botCoreEvent;
        this.botWorkflowHandler = botWorkflowHandler;
    }

    @SneakyThrows
    @Override
    public boolean preHandle(Bot bot, MessageEvent event) {
        Long userId = event.getUserId();
        if (botCoreEvent.getBotQqs().contains(userId)) {
            log.debug("[{}]拦截到bot消息: , 发送消息的bot: {}", bot.getSelfId(), userId);
            return false;
        }
        return true;
    }

    @Override
    public void afterCompletion(Bot bot, MessageEvent event){}

    @SneakyThrows
    @GroupMessageHandler
    @BotEvent(
        name = "群消息",
        description = "当 BOT 收到群消息时触发",
        order = 1,
        eventType = "GroupMessage",
        entityName = "GroupMessageEvent"
    )
    @EventParam(name = "botId", description = "BOT的QQ号", order = 1, type = "Long")
    @EventParam(name = "groupId", description = "群号", order = 2, type = "Long")
    @EventParam(name = "userId", description = "发送者的 QQ 号", order = 3, type = "Long")
    @EventParam(name = "message", description = "消息内容", order = 4, example = "你好")
    @EventParam(name = "messageId", description = "消息ID", order = 5, type = "Long")
    @EventParam(name = "isAt", description = "是否@了BOT", order = 6, type = "Boolean")
    public void groupMessage(Bot bot, GroupMessageEvent event){
        log.debug("[群消息][BOT:{}] 群号: {}, 发送者: {}, 消息内容: {}",
                bot.getSelfId(), event.getGroupId(), event.getUserId(), event.getMessage());
        botWorkflowHandler.handleBotEvent(bot.getSelfId(), "GroupMessage", event);
    }

    @SneakyThrows
    @PrivateMessageHandler
    @BotEvent(
        name = "私聊消息",
        description = "当 BOT 收到私聊消息时触发",
        order = 2,
        eventType = "PrivateMessage",
        entityName = "PrivateMessageEvent"
    )
    @EventParam(name = "botId", description = "BOT 的 QQ 号", order = 1, type = "Long")
    @EventParam(name = "userId", description = "发送者的 QQ 号", order = 2, type = "Long")
    @EventParam(name = "message", description = "消息内容", order = 3, example = "你好")
    @EventParam(name = "messageId", description = "消息ID", order = 4, type = "Long")
    public void privateMessage(Bot bot, PrivateMessageEvent event){
        log.debug("[私聊消息][BOT:{}] 发送者: {}, 消息内容: {}",
                bot.getSelfId(), event.getUserId(), event.getMessage());
        botWorkflowHandler.handleBotEvent(bot.getSelfId(), "PrivateMessage", event);
    }

    @SneakyThrows
    @GroupDecreaseHandler
    @BotEvent(
        name = "群成员减少",
        description = "当有成员退出群时触发",
        order = 3,
        eventType = "GroupDecrease",
        entityName = "GroupDecreaseNoticeEvent"
    )
    @EventParam(name = "botId", description = "BOT 的 QQ 号", order = 1, type = "Long")
    @EventParam(name = "groupId", description = "群号", order = 2, type = "Long")
    @EventParam(name = "userId", description = "被处理人ID（退群成员的 QQ 号）", order = 3, type = "Long")
    @EventParam(name = "operatorId", description = "操作人ID（踢人者的 QQ 号）", order = 4, type = "Long")
    public void groupDecreaseHandler(Bot bot, GroupDecreaseNoticeEvent event){
        log.debug("[群成员减少][BOT:{}] 群号: {}, 处理人: {}, 被处理人: {}",
                bot.getSelfId(), event.getGroupId(), event.getOperatorId(), event.getUserId());
        botWorkflowHandler.handleBotEvent(bot.getSelfId(), "GroupDecrease", event);
    }

    @SneakyThrows
    @GroupIncreaseHandler
    @BotEvent(
        name = "群成员增加",
        description = "当有新成员加入群时触发",
        order = 4,
        eventType = "GroupIncrease",
        entityName = "GroupIncreaseNoticeEvent"
    )
    @EventParam(name = "botId", description = "BOT 的 QQ 号", order = 1, type = "Long")
    @EventParam(name = "groupId", description = "群号", order = 2, type = "Long")
    @EventParam(name = "userId", description = "被处理人ID（新成员的 QQ 号）", order = 3, type = "Long")
    @EventParam(name = "operatorId", description = "操作人ID（邀请人的 QQ 号）", order = 4, type = "Long")
    public void groupIncreaseHandler(Bot bot, GroupIncreaseNoticeEvent event){
        log.debug("[群成员增加][BOT:{}] 群号: {}, 处理人: {}, 被处理人: {}",
                bot.getSelfId(), event.getGroupId(), event.getOperatorId(), event.getUserId());
        botWorkflowHandler.handleBotEvent(bot.getSelfId(), "GroupIncrease", event);
    }

    @SneakyThrows
    @GroupAddRequestHandler
    @BotEvent(
        name = "群加请求",
        description = "当有人申请加群时触发",
        order = 5,
        eventType = "GroupAddRequest",
        entityName = "GroupAddRequestEvent"
    )
    @EventParam(name = "botId", description = "BOT 的 QQ 号", order = 1, type = "Long")
    @EventParam(name = "groupId", description = "群号", order = 2, type = "Long")
    @EventParam(name = "userId", description = "申请人的 QQ 号", order = 3, type = "Long")
    @EventParam(name = "invitorId", description = "邀请人ID", order = 4, type = "Long")
    @EventParam(name = "comment", description = "加群请求消息", order = 5)
    public void groupAddRequestHandler(Bot bot, GroupAddRequestEvent event){
        log.debug("[群加请求][BOT:{}] 群号: {}, 申请人: {}, 邀请人: {}, 请求内容：{}",
                bot.getSelfId(), event.getGroupId(), event.getUserId(), event.getInvitorId(),event.getComment());
        botWorkflowHandler.handleBotEvent(bot.getSelfId(), "GroupAddRequest", event);
    }
}
