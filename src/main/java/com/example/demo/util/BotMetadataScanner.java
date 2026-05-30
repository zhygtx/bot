package com.example.demo.util;

import com.example.demo.annotation.ActionParam;
import com.example.demo.annotation.BotAction;
import com.example.demo.annotation.BotEvent;
import com.example.demo.annotation.EventField;
import com.example.demo.api.BotActionService;
import com.example.demo.metadata.ActionMetadata;
import com.example.demo.metadata.EventMetadata;
import com.example.demo.metadata.FieldMetadata;
import com.example.demo.pojo.plugin.ParameterInfo;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * BOT 元数据扫描器
 * 自动扫描并注册所有带有注解的 BOT 事件和动作
 */
@Component
@Slf4j
public class BotMetadataScanner {
    
    private final ApplicationContext applicationContext;
    
    private final List<EventMetadata> eventMetadataList = new ArrayList<>();
    private final List<ActionMetadata> actionMetadataList = new ArrayList<>();
    
    public BotMetadataScanner(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    @PostConstruct
    public void init() {
        scanEvents();
        scanActions();
        log.info("BOT元数据扫描完成: {}个事件, {}个动作", 
                eventMetadataList.size(), actionMetadataList.size());
    }
    
    /**
     * 扫描所有事件类
     */
    private void scanEvents() {
        // 获取所有带有 @BotEvent 注解的 Bean
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(BotEvent.class);
        
        for (Object bean : beans.values()) {
            Class<?> clazz = bean.getClass();
            
            // 如果当前类没有注解，尝试获取父类的注解
            if (!clazz.isAnnotationPresent(BotEvent.class) && clazz.getSuperclass() != null) {
                clazz = clazz.getSuperclass();
            }
            
            BotEvent botEvent = clazz.getAnnotation(BotEvent.class);
            if (botEvent != null) {
                EventMetadata metadata = extractEventMetadata(clazz, botEvent);
                eventMetadataList.add(metadata);
                log.info("扫描到BOT事件: {} ({})", botEvent.name(), botEvent.type());
            }
        }
        
        // 按 order 排序
        eventMetadataList.sort(Comparator.comparingInt(EventMetadata::getOrder));
    }
    
    /**
     * 提取事件元数据
     */
    private EventMetadata extractEventMetadata(Class<?> clazz, BotEvent botEvent) {
        List<FieldMetadata> fields = new ArrayList<>();
        
        // 遍历类及其父类的所有字段
        Class<?> currentClass = clazz;
        while (currentClass != null && !currentClass.equals(Object.class)) {
            for (Field field : currentClass.getDeclaredFields()) {
                EventField eventField = field.getAnnotation(EventField.class);
                if (eventField != null) {
                    FieldMetadata fieldMetadata = FieldMetadata.builder()
                            .fieldName(field.getName())
                            .fieldType(getSimpleTypeName(field.getType()))
                            .description(eventField.description())
                            .order(eventField.order())
                            .inherited(eventField.inherited())
                            .build();
                    fields.add(fieldMetadata);
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        
        // 按 order 排序
        fields.sort(Comparator.comparingInt(FieldMetadata::getOrder));
        
        EventMetadata.EntityMetadata entityInfo = EventMetadata.EntityMetadata.builder()
                .entityName(clazz.getSimpleName())
                .fields(fields)
                .build();
        
        return EventMetadata.builder()
                .eventType(botEvent.type())
                .eventName(botEvent.name())
                .description(botEvent.description())
                .order(botEvent.order())
                .entityInfo(entityInfo)
                .build();
    }
    
    /**
     * 扫描所有动作方法
     */
    private void scanActions() {
        try {
            Class<BotActionService> serviceClass = BotActionService.class;
            
            for (Method method : serviceClass.getDeclaredMethods()) {
                BotAction botAction = method.getAnnotation(BotAction.class);
                if (botAction != null) {
                    ActionMetadata metadata = extractActionMetadata(method, botAction);
                    actionMetadataList.add(metadata);
                    log.info("扫描到BOT动作: {} ({})", botAction.name(), method.getName());
                }
            }
            
            // 按 order 排序
            actionMetadataList.sort(Comparator.comparingInt(ActionMetadata::getOrder));
        } catch (Exception e) {
            log.error("扫描BOT动作失败", e);
        }
    }
    
    /**
     * 提取动作元数据
     */
    private ActionMetadata extractActionMetadata(Method method, BotAction botAction) {
        List<ParameterInfo> parameters = new ArrayList<>();
        Parameter[] methodParams = method.getParameters();
        
        for (int i = 0; i < methodParams.length; i++) {
            Parameter param = methodParams[i];
            ActionParam actionParam = param.getAnnotation(ActionParam.class);
            
            String description = actionParam != null ? actionParam.description() : "参数";
            int order = actionParam != null ? actionParam.order() : i;
            
            ParameterInfo paramInfo = ParameterInfo.builder()
                    .id(String.valueOf(i + 1))
                    .name(param.getName())
                    .type(getSimpleTypeName(param.getType()))
                    .description(description)
                    .order(order)
                    .build();
            parameters.add(paramInfo);
        }
        
        return ActionMetadata.builder()
                .actionName(method.getName())
                .actionDisplayName(botAction.name())
                .description(botAction.description())
                .order(botAction.order())
                .parameters(parameters)
                .build();
    }
    
    /**
     * 获取类型的简化名称
     */
    private String getSimpleTypeName(Class<?> clazz) {
        if (clazz.isArray()) {
            return getSimpleTypeName(clazz.getComponentType()) + "[]";
        }
        
        String name = clazz.getSimpleName();
        
        // 基本类型处理
        if ("boolean".equals(name)) return "boolean";
        if ("byte".equals(name)) return "byte";
        if ("short".equals(name)) return "short";
        if ("int".equals(name)) return "int";
        if ("long".equals(name)) return "Long";
        if ("float".equals(name)) return "float";
        if ("double".equals(name)) return "double";
        if ("char".equals(name)) return "char";
        
        return name;
    }
    
    /**
     * 获取所有事件元数据
     */
    public List<EventMetadata> getAllEvents() {
        return Collections.unmodifiableList(eventMetadataList);
    }
    
    /**
     * 获取所有动作元数据
     */
    public List<ActionMetadata> getAllActions() {
        return Collections.unmodifiableList(actionMetadataList);
    }
}
