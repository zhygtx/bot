package com.example.demo.annotation;

import java.lang.annotation.*;

/**
 * 字段展示信息注解。
 * <p>
 * 标注在实体类的字段上，为前端提供可读的字段描述。
 * 适用于 BotAction 返回值实体类 和 自定义参数实体类 的字段描述。
 * <p>
 * 扫描器查找字段描述的优先级：
 * <ol>
 *   <li>{@code @DisplayField} 注解（用户自定义类）</li>
 *   <li>{@link com.example.demo.handler.scanner.EventFieldDescription EventFieldDescription} 集中 Map（SDK 固定类型）</li>
 *   <li>从字段名自动生成（兜底）</li>
 * </ol>
 *
 * 使用示例：
 * <pre>{@code
 * public class MyData {
 *     @DisplayField(value = "用户ID", required = true)
 *     private Long userId;
 *
 *     @DisplayField("昵称")
 *     private String nickname;
 *
 *     @DisplayField(value = "创建时间", example = "2024-01-01 12:00:00")
 *     private LocalDateTime createTime;
 * }
 * }</pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DisplayField {

    /** 字段描述 */
    String value();

    /** 是否必须 */
    boolean required() default false;

    /** 示例值 */
    String example() default "";
}
