package com.example.demo.annotation;

import java.lang.annotation.*;

/**
 * 事件字段注解
 * 用于标记事件类中的字段，提供字段的元数据信息
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EventField {
    
    /**
     * 字段描述
     */
    String description() default "";
    
    /**
     * 排序权重（越小越靠前）
     */
    int order() default 0;
    
    /**
     * 是否为继承字段
     */
    boolean inherited() default false;
}
