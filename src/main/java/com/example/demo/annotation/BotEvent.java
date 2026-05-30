package com.example.demo.annotation;

import java.lang.annotation.*;

/**
 * BOT 事件注解
 * 用于标记事件类，提供事件的元数据信息
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BotEvent {
    
    /**
     * 事件类型标识
     */
    String type();
    
    /**
     * 事件显示名称
     */
    String name();
    
    /**
     * 事件描述
     */
    String description();
    
    /**
     * 排序权重（越小越靠前）
     */
    int order() default 0;
}
