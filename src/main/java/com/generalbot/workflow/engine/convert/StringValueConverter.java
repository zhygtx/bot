package com.generalbot.workflow.engine.convert;

import org.springframework.stereotype.Component;

/**
 * String 例外转换：目标参数为 String 时，非 String 值在入参前自动 toString()。
 */
@Component
public class StringValueConverter implements ValueConverter {

    /**
     * 顺序较低，允许后续注册更具体的转换器优先处理。
     */
    @Override
    public int order() {
        return 100;
    }

    /**
     * 仅当目标类型是 String 且当前值不是 String 时执行。
     */
    @Override
    public boolean canConvert(Object value, String targetType) {
        return value != null && !(value instanceof String) && isStringType(targetType);
    }

    /**
     * 直接使用 String.valueOf 转为字符串。
     */
    @Override
    public Object convert(Object value, String targetType) {
        return String.valueOf(value);
    }

    /**
     * 判断类型是否为 String（兼容全限定名 java.lang.String）。
     */
    private boolean isStringType(String type) {
        if (type == null || type.isBlank()) {
            return false;
        }
        int dot = type.lastIndexOf('.');
        String simpleName = dot >= 0 ? type.substring(dot + 1) : type;
        return "String".equals(simpleName);
    }
}
