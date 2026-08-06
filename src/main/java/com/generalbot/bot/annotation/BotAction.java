package com.generalbot.bot.annotation;

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

    /**
     * 分类名称数组，支持多分类。
     * 用于前端分组展示，如 "消息操作"、"群组操作"、"系统工具"。
     * <p>
     * 使用示例：
     * <pre>
     * categories = {"消息操作", "系统工具"}
     * categories = "消息操作"        // 单分类可省略大括号
     * </pre>
     */
    String[] categories() default {};

    /**
     * 分类排序数组，与 categories 一一对应，越小越靠前。
     * 如果长度小于 categories，剩余分类使用默认值 0。
     * <p>
     * 使用示例：
     * <pre>
     * categories = {"消息操作", "系统工具"}, categoryOrders = {0, 5}
     * </pre>
     */
    int[] categoryOrders() default {};

    /**
     * 返回值描述，用于前端展示。
     * <p>
     * 仅对基本类型 / String / 自定义 POJO 等非 void 返回值生效。
     * SDK 类型因字段自动扫描已有描述，此参数作为兜底。
     * 示例：{@code returnDescription = "群名称"}、{@code returnDescription = "是否在线"}
     */
    String returnDescription() default "";
}
