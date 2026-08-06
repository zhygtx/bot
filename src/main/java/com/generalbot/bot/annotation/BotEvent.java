package com.generalbot.bot.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BotEvent {

    /** 事件名称，用于前端展示 */
    String name();

    /** 事件描述，用于前端展示 */
    String description() default "";

    /** 事件排序，越小越靠前 */
    int order() default 0;

    /**
     * 分类名称数组，支持多分类。
     * 用于前端分组展示，如 "消息事件"、"通知事件"、"请求事件"、"元事件"。
     * <p>
     * 使用示例：
     * <pre>
     * categories = {"消息事件", "系统事件"}
     * categories = "消息事件"        // 单分类可省略大括号
     * </pre>
     */
    String[] categories() default {};

    /**
     * 分类排序数组，与 categories 一一对应，越小越靠前。
     * 如果长度小于 categories，剩余分类使用默认值 0。
     * <p>
     * 使用示例：
     * <pre>
     * categories = {"消息事件", "系统事件"}, categoryOrders = {0, 5}
     * </pre>
     */
    int[] categoryOrders() default {};
}
