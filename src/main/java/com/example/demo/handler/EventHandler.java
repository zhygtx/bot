package com.example.demo.handler;

import com.example.demo.core.processor.MessageProcessor;
import com.example.demo.core.processor.TaskProcessor;
import com.example.demo.pojo.event.EventLog;
import com.example.demo.pojo.event.GroupEvent;
import com.example.demo.pojo.event.GroupMsg;
import com.example.demo.pojo.event.PrivateMsg;
import com.example.demo.service.EventLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.List;


/**
 * 事件处理类
 * ↓↓ 请在以下位置添加枚举
 * @see com.example.demo.core.engine.RoleEngine#getActions(List, Object) -> 枚举处添加事件
 * @see com.example.demo.core.engine.ScopeEngine#getRoles(Object)  -> 枚举处添加动作
 * @see com.example.demo.pojo.task.Role -> 枚举处添加任务
 * @see com.example.demo.pojo.event.Event -> 枚举处添加事件
 * ->数据库Role表添加任务枚举
 */
@Slf4j
@Component("demoEventHandler")
@Shiro
public class EventHandler implements BotMessageEventInterceptor {

    private final BotCoreEvent botCoreEvent;
    private final TaskProcessor taskProcessor;
    private final MessageProcessor messageProcessor;
    private final EventLogService eventLogService;
    private final ObjectMapper objectMapper;

    public EventHandler(BotCoreEvent botCoreEvent, TaskProcessor taskProcessor, MessageProcessor messageProcessor, EventLogService eventLogService, ObjectMapper objectMapper) {
        this.botCoreEvent = botCoreEvent;
        this.taskProcessor = taskProcessor;
        this.messageProcessor = messageProcessor;
        this.eventLogService = eventLogService;
        this.objectMapper = objectMapper;
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
    @SneakyThrows
    @GroupMessageHandler
    public void groupMessage(Bot bot, GroupMessageEvent event){
        log.debug("[群消息][BOT:{}] 群号: {}, 发送者: {}, 消息内容: {}",
                bot.getSelfId(), event.getGroupId(), event.getUserId(), event.getMessage());
        GroupMsg groupMsg = messageProcessor.groupProcess(bot, event);
        taskProcessor.taskProcess(bot, groupMsg);
        eventLogService.insert(EventLog.builder()
                .selfId(bot.getSelfId())
                .type("GroupMessageEvent")
                .subType(event.getSubType())
                .time(event.getTime())
                .userId(event.getUserId())
                .groupId(event.getGroupId())
                .eventData(objectMapper.writeValueAsString(event))
                .build()
        );
    }

    @SneakyThrows
    @PrivateMessageHandler
    public void privateMessage(Bot bot, PrivateMessageEvent event){
        log.debug("[私聊消息][BOT:{}] 发送者: {}, 消息内容: {}",
                bot.getSelfId(), event.getUserId(), event.getMessage());
        PrivateMsg privateMsg = messageProcessor.privateProcess(bot, event);
        taskProcessor.taskProcess(bot, privateMsg);
        eventLogService.insert(EventLog.builder()
                .selfId(bot.getSelfId())
                .type("PrivateMessageEvent")
                .subType(event.getSubType())
                .time(event.getTime())
                .userId(event.getUserId())
                .eventData(objectMapper.writeValueAsString(event))
                .build()
        );
    }

    /**
     * 群成员减少事件
     * @param bot   Bot实例，用于与服务器通信
     * @param event 群成员减少事件对象，包含事件详细信息
     */
    @SneakyThrows
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
                .data(event)
                .build();
        taskProcessor.taskProcess(bot,groupEvent);
        eventLogService.insert(EventLog.builder()
                .selfId(bot.getSelfId())
                .type("GroupDecreaseNoticeEvent")
                .subType(event.getSubType())
                .time(event.getTime())
                .userId(event.getUserId())
                .groupId(event.getGroupId())
                .eventData(objectMapper.writeValueAsString(event))
                .build()
        );
    }

    /**
     * 群成员增加事件
     * @param bot   Bot实例，用于与服务器通信
     * @param event 群成员增加事件对象，包含事件详细信息
     */
    @SneakyThrows
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
                .data(event)
                .build();
        taskProcessor.taskProcess(bot,groupEvent);
        eventLogService.insert(EventLog.builder()
                .selfId(bot.getSelfId())
                .type("GroupIncreaseNoticeEvent")
                .subType(event.getSubType())
                .time(event.getTime())
                .userId(event.getUserId())
                .groupId(event.getGroupId())
                .eventData(objectMapper.writeValueAsString(event))
                .build()
        );
    }

    /**
     * 群成员增加请求事件
     * @param bot   Bot实例，用于与服务器通信
     * @param event 群成员增加请求事件对象，包含事件详细信息
     */
    @SneakyThrows
    @GroupAddRequestHandler
    public void GroupAddRequestHandler(Bot bot, GroupAddRequestEvent event){
        GroupEvent groupEvent = GroupEvent.builder()
                .eventType(GroupEvent.Type.GroupAddRequest)
                .botId(bot.getSelfId())
                .groupId(event.getGroupId())
                .operatorId(event.getInvitorId())
                .userId(event.getUserId())
                .data(event)
                .build();
        taskProcessor.taskProcess(bot,groupEvent);
        eventLogService.insert(EventLog.builder()
                .selfId(bot.getSelfId())
                .type("GroupAddRequestEvent")
                .subType(event.getSubType())
                .time(event.getTime())
                .userId(event.getUserId())
                .groupId(event.getGroupId())
                .eventData(objectMapper.writeValueAsString(event))
                .build()
        );
    }
}
