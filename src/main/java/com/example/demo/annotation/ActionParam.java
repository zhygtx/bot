package com.example.demo.annotation;

import java.lang.annotation.*;

/**
 * 动作参数注解
 * 用于标记接口方法的参数，提供参数的元数据信息
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ActionParam {
    
    /**
     * 参数描述
     */
    String description() default "";
    
    /**
     * 排序权重（越小越靠前）
     */
    int order() default 0;
}
