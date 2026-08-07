package com.generalbot.workflow.engine.convert;

/**
 * 入参值转换器。用于“不做隐式类型转换”原则下明确声明的例外转换。
 * 新增转换能力时实现该接口并注册为 Spring Bean 即可。
 */
public interface ValueConverter {

    /**
     * 转换器顺序，值越小越优先。
     * @return 顺序
     */
    int order();

    /**
     * 判断当前值是否需要转换为目标参数类型。
     * @param value 入参值
     * @param targetType 目标参数类型
     * @return 是否支持转换
     */
    boolean canConvert(Object value, String targetType);

    /**
     * 执行转换。
     * @param value 入参值
     * @param targetType 目标参数类型
     * @return 转换后的值
     */
    Object convert(Object value, String targetType);
}
