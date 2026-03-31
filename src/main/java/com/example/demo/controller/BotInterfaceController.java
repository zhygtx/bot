package com.example.demo.controller;

import com.example.demo.pojo.Result;
import com.example.demo.pojo.event.Event;
import com.example.demo.pojo.plugin.ParameterInfo;
import com.example.demo.util.BotActionScanner;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * BOT 接口控制器
 * 提供前端获取 BOT 事件与 BOT 动作相关方法内容的接口
 */
@Slf4j
@RestController
@RequestMapping("/api/bot")
public class BotInterfaceController {

    private final BotActionScanner botActionScanner;

    public BotInterfaceController(BotActionScanner botActionScanner) {
        this.botActionScanner = botActionScanner;
    }

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
        
        // 定时事件
        events.add(createEventInfo("scheduledEvent", "定时触发", "按设定时间自动触发工作流", createScheduledEventEntityInfo()));
        
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
     * 获取定时事件实体类信息
     * 定时任务作为触发器，没有返回值
     */
    private EntityInfo createScheduledEventEntityInfo() {
        // 定时任务没有返回值，返回空字段列表
        return createEntityInfo("ScheduledEvent", new ArrayList<>());
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
        
        // 从 BotActionScanner 获取所有 BOT 动作方法
        Map<String, BotActionScanner.BotActionMethodInfo> botActionMethods = botActionScanner.getAllBotActionMethods();
        
        for (Map.Entry<String, BotActionScanner.BotActionMethodInfo> entry : botActionMethods.entrySet()) {
            String methodName = entry.getKey();
            BotActionScanner.BotActionMethodInfo methodInfo = entry.getValue();
            
            // 创建参数信息列表
            List<ParameterInfo> parameters = new ArrayList<>();
            String[] paramNames = methodInfo.paramNames();
            String[] paramTypes = methodInfo.paramTypes();
            
            for (int i = 0; i < paramNames.length; i++) {
                String paramName = paramNames[i];
                String paramType = paramTypes[i];
                String description = getParamDescription(methodName, paramName);
                parameters.add(createParameterInfo(String.valueOf(i + 1), paramName, paramType, description, i + 1));
            }
            
            // 创建动作信息
            String actionDisplayName = getActionDisplayName(methodName);
            String description = getActionDescription(methodName);
            actions.add(createActionInfo(methodName, actionDisplayName, description, parameters));
        }
        
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
     * 获取参数描述
     */
    private String getParamDescription(String methodName, String paramName) {
        switch (methodName) {
            case "sendGroupMsg":
                switch (paramName) {
                    case "botQQ":
                        return "BOT 的 QQ 号";
                    case "groupId":
                        return "群号";
                    case "msg":
                        return "消息内容";
                }
                break;
            case "sendPrivateMsg":
                switch (paramName) {
                    case "botQQ":
                        return "BOT 的 QQ 号";
                    case "userId":
                        return "用户的 QQ 号";
                    case "msg":
                        return "消息内容";
                }
                break;
            case "sendGroupMsgBatch":
                switch (paramName) {
                    case "botQQ":
                        return "BOT 的 QQ 号";
                    case "msg":
                        return "群消息，key 为群号，value 为消息内容";
                }
                break;
            case "sendPrivateMsgBatch":
                switch (paramName) {
                    case "botQQ":
                        return "BOT 的 QQ 号";
                    case "msg":
                        return "私聊消息，key 为用户 QQ，value 为消息内容";
                }
                break;
        }
        return "参数";
    }
    
    /**
     * 获取动作显示名称
     */
    private String getActionDisplayName(String methodName) {
        return switch (methodName) {
            case "sendGroupMsg" -> "发送群消息";
            case "sendPrivateMsg" -> "发送私聊消息";
            case "sendGroupMsgBatch" -> "批量发送群消息";
            case "sendPrivateMsgBatch" -> "批量发送私聊消息";
            default -> methodName;
        };
    }
    
    /**
     * 获取动作描述
     */
    private String getActionDescription(String methodName) {
        return switch (methodName) {
            case "sendGroupMsg" -> "向指定群发送消息";
            case "sendPrivateMsg" -> "向指定用户发送私聊消息";
            case "sendGroupMsgBatch" -> "批量向多个群发送消息";
            case "sendPrivateMsgBatch" -> "批量向多个用户发送私聊消息";
            default -> "BOT 动作";
        };
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