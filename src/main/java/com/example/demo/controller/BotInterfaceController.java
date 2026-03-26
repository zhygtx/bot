package com.example.demo.controller;

import com.example.demo.pojo.Result;
import com.example.demo.pojo.event.Event;
import com.example.demo.pojo.plugin.ParameterInfo;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * BOT 接口控制器
 * 提供前端获取 BOT 事件与 BOT 动作相关方法内容的接口
 */
@Slf4j
@RestController
@RequestMapping("/api/bot")
public class BotInterfaceController {

    /**
     * 获取 BOT 事件列表
     * @return BOT 事件列表
     */
    @GetMapping("/events")
    public Result<?> getBotEvents() {
        List<EventInfo> events = new ArrayList<>();
        
        // 群消息事件
        events.add(createEventInfo(Event.Type.GroupMsg.name(), "群消息", "当 BOT 收到群消息时触发", createGroupMsgEntityInfo()));
        
        // 私聊消息事件
        events.add(createEventInfo(Event.Type.PrivateMsg.name(), "私聊消息", "当 BOT 收到私聊消息时触发", createPrivateMsgEntityInfo()));
        
        // 群成员增加事件
        events.add(createEventInfo(Event.Type.GroupIncrease.name(), "群成员增加", "当群成员增加时触发", createGroupEventEntityInfo()));
        
        // 群成员减少事件
        events.add(createEventInfo(Event.Type.GroupDecrease.name(), "群成员减少", "当群成员减少时触发", createGroupEventEntityInfo()));
        
        // 群加请求事件
        events.add(createEventInfo(Event.Type.GroupAddRequest.name(), "群加请求", "当收到群加请求时触发", createGroupEventEntityInfo()));
        
        return Result.success(null, events);
    }
    
    /**
     * 创建事件信息
     */
    private EventInfo createEventInfo(String eventType, String eventName, String description, EntityInfo entityInfo) {
        return EventInfo.builder()
                .eventType(eventType)
                .eventName(eventName)
                .description(description)
                .entityInfo(entityInfo)
                .build();
    }
    
    /**
     * 获取群消息实体类信息
     */
    private EntityInfo createGroupMsgEntityInfo() {
        List<FieldInfo> fields = new ArrayList<>();
        addEventBaseFields(fields);
        addMsgFields(fields);
        fields.add(createFieldInfo("isAt", "boolean", "是否@了BOT"));
        fields.add(createFieldInfo("userRole", "String", "发送者用户权限"));
        
        return createEntityInfo("GroupMsg", fields);
    }
    
    /**
     * 获取私聊消息实体类信息
     */
    private EntityInfo createPrivateMsgEntityInfo() {
        List<FieldInfo> fields = new ArrayList<>();
        addEventBaseFields(fields);
        addMsgFields(fields);
        
        return createEntityInfo("PrivateMsg", fields);
    }
    
    /**
     * 获取群事件实体类信息
     */
    private EntityInfo createGroupEventEntityInfo() {
        List<FieldInfo> fields = new ArrayList<>();
        addEventBaseFields(fields);
        fields.add(createFieldInfo("operatorId", "Long", "操作人的 QQ 号"));
        
        return createEntityInfo("GroupEvent", fields);
    }
    
    /**
     * 添加 Event 基类字段
     */
    private void addEventBaseFields(List<FieldInfo> fields) {
        fields.add(createFieldInfo("botQQ", "Long", "BOT 的 QQ 号"));
        fields.add(createFieldInfo("userId", "Long", "用户的 QQ 号"));
        fields.add(createFieldInfo("groupId", "Long", "群号"));
        fields.add(createFieldInfo("eventType", "Event.Type", "事件类型"));
        fields.add(createFieldInfo("data", "Object", "事件原本数据"));
    }
    
    /**
     * 添加 Msg 类字段
     */
    private void addMsgFields(List<FieldInfo> fields) {
        fields.add(createFieldInfo("content", "Map<Integer, Map<String, Object>>", "消息内容"));
        fields.add(createFieldInfo("type", "List<String>", "消息类型"));
    }
    
    /**
     * 创建字段信息
     */
    private FieldInfo createFieldInfo(String fieldName, String fieldType, String description) {
        return FieldInfo.builder()
                .fieldName(fieldName)
                .fieldType(fieldType)
                .description(description)
                .build();
    }
    
    /**
     * 创建实体类信息
     */
    private EntityInfo createEntityInfo(String entityName, List<FieldInfo> fields) {
        return EntityInfo.builder()
                .entityName(entityName)
                .fields(fields)
                .build();
    }
    
    /**
     * 获取 BOT 动作列表
     * @return BOT 动作列表
     */
    @GetMapping("/actions")
    public Result<?> getBotActions() {
        List<ActionInfo> actions = new ArrayList<>();
        
        // 发送群消息动作
        actions.add(createActionInfo("sendGroupMsg", "发送群消息", "向指定群发送消息", createSendGroupMsgParameters()));
        
        // 发送私聊消息动作
        actions.add(createActionInfo("sendPrivateMsg", "发送私聊消息", "向指定用户发送私聊消息", createSendPrivateMsgParameters()));
        
        return Result.success(null, actions);
    }
    
    /**
     * 创建动作信息
     */
    private ActionInfo createActionInfo(String actionName, String actionDisplayName, String description, List<ParameterInfo> parameters) {
        return ActionInfo.builder()
                .actionName(actionName)
                .actionDisplayName(actionDisplayName)
                .description(description)
                .parameters(parameters)
                .build();
    }
    
    /**
     * 获取发送群消息动作参数
     */
    private List<ParameterInfo> createSendGroupMsgParameters() {
        List<ParameterInfo> parameters = new ArrayList<>();
        parameters.add(createParameterInfo("1", "botQQ", "Long", "BOT 的 QQ 号", 1));
        parameters.add(createParameterInfo("2", "groupId", "Long", "群号", 2));
        parameters.add(createParameterInfo("3", "msg", "String", "消息内容", 3));
        return parameters;
    }
    
    /**
     * 获取发送私聊消息动作参数
     */
    private List<ParameterInfo> createSendPrivateMsgParameters() {
        List<ParameterInfo> parameters = new ArrayList<>();
        parameters.add(createParameterInfo("1", "botQQ", "Long", "BOT 的 QQ 号", 1));
        parameters.add(createParameterInfo("2", "userId", "Long", "用户的 QQ 号", 2));
        parameters.add(createParameterInfo("3", "msg", "String", "消息内容", 3));
        return parameters;
    }
    
    /**
     * 创建参数信息
     */
    private ParameterInfo createParameterInfo(String id, String name, String type, String description, int order) {
        return ParameterInfo.builder()
                .id(id)
                .name(name)
                .type(type)
                .description(description)
                .order(order)
                .build();
    }
    
    /**
     * BOT 事件信息
     */
    @Builder
    @Data
    public static class EventInfo {
        private String eventType;
        private String eventName;
        private String description;
        private EntityInfo entityInfo;
    }
    
    /**
     * 实体类信息
     */
    @Builder
    @Data
    public static class EntityInfo {
        private String entityName;
        private List<FieldInfo> fields;
    }
    
    /**
     * 字段信息
     */
    @Builder
    @Data
    public static class FieldInfo {
        private String fieldName;
        private String fieldType;
        private String description;
    }
    
    /**
     * BOT 动作信息
     */
    @Builder
    @Data
    public static class ActionInfo {
        private String actionName;
        private String actionDisplayName;
        private String description;
        private List<ParameterInfo> parameters;
    }
}