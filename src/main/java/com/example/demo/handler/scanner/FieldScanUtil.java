package com.example.demo.handler.scanner;

import com.example.demo.annotation.DisplayField;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * 字段扫描共用工具。
 * <p>
 * 提供字段描述的三级查找策略和反射字段提取方法，
 * 供 {@link BotEventScanner} 和 {@link BotActionScanner} 复用。
 */
final class FieldScanUtil {

    private FieldScanUtil() {}

    // ==================== 描述 ====================

    /**
     * 解析字段描述（三级优先级）：
     * <ol>
     *   <li>{@link DisplayField @DisplayField} 注解</li>
     *   <li>{@link EventFieldDescription EventFieldDescription} 集中 Map</li>
     *   <li>camelCase 自动分词</li>
     * </ol>
     *
     * @param declaringClass 声明字段的类
     * @param field          字段
     * @return 字段描述，永不返回 null
     */
    static String resolveFieldDescription(Class<?> declaringClass, Field field) {
        // 第一优先级：@DisplayField
        DisplayField df = field.getAnnotation(DisplayField.class);
        if (df != null) return df.value();

        // 第二优先级：集中 Map
        String desc = EventFieldDescription.tryGet(declaringClass.getSimpleName(), field.getName());
        if (desc != null) return desc;

        // 第三优先级：自动生成
        return EventFieldDescription.resolve(declaringClass.getSimpleName(), field.getName());
    }

    // ==================== JSON 名称 ====================

    /**
     * 获取字段对应的 JSON 字段名。
     * 优先使用 {@link JsonProperty @JsonProperty} 注解的值，回退到 Java 字段名。
     */
    static String getJsonFieldName(Field field) {
        JsonProperty jp = field.getAnnotation(JsonProperty.class);
        return (jp != null) ? jp.value() : field.getName();
    }

    // ==================== 过滤 ====================

    /**
     * 判断字段是否应被扫描。
     * 跳过：static、transient、synthetic、serialVersionUID。
     */
    static boolean isScannableField(Field field) {
        int mod = field.getModifiers();
        if (Modifier.isStatic(mod)) return true;
        if (Modifier.isTransient(mod)) return true;
        if (field.isSynthetic()) return true;
        return "serialVersionUID".equals(field.getName());
    }

    // ==================== 类型判断 ====================

    /** 判断是否为简单类型（基本类型、包装类、String） */
    static boolean isSimpleType(Class<?> clazz) {
        if (clazz.isPrimitive()) return true;
        if (clazz == String.class) return true;
        if (Number.class.isAssignableFrom(clazz)) return true;
        if (Boolean.class == clazz) return true;
        if (Character.class == clazz) return true;
        return clazz.isEnum();
    }

    /** 获取类型简名 */
    static String getSimpleTypeName(Class<?> clazz) {
        if (clazz.isArray()) {
            return getSimpleTypeName(clazz.getComponentType()) + "[]";
        }
        return clazz.getSimpleName();
    }
}
