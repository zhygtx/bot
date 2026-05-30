package com.example.demo.annotation;

import java.lang.annotation.*;

/**
 * BOT 动作注解
 * 用于标记接口方法，提供动作的元数据信息
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BotAction {
    
    /**
     * 动作显示名称
     */
    String name();
    
    /**
     * 动作描述
     */
    String description();
    
    /**
     * 排序权重（越小越靠前）
     */
    int order() default 0;
}
