package com.generalbot.workflow.engine.convert;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * 值转换器注册表。按 order 升序执行，命中第一个可转换的转换器即返回，避免多次隐式转换叠加。
 */
@Component
public class ValueConverterRegistry {

    private final List<ValueConverter> converters;

    /**
     * 构造时收集全部 ValueConverter Bean 并按顺序排序。
     * @param converters 转换器列表
     */
    public ValueConverterRegistry(List<ValueConverter> converters) {
        this.converters = converters.stream()
                .sorted(Comparator.comparingInt(ValueConverter::order))
                .toList();
    }

    /**
     * 尝试将入参值转换为目标参数类型；没有转换器命中时原样返回。
     * @param value 入参值
     * @param targetType 目标参数类型
     * @return 转换后的值
     */
    public Object convert(Object value, String targetType) {
        for (ValueConverter converter : converters) {
            if (converter.canConvert(value, targetType)) {
                return converter.convert(value, targetType);
            }
        }
        return value;
    }
}
