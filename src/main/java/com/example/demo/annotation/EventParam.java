package com.example.demo.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(EventParams.class)
@Documented
public @interface EventParam {

    String name();

    String description() default "";

    int order() default 0;

    boolean required() default false;

    String type() default "String";

    String example() default "";
}
