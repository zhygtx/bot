package com.generalbot.workflow.entity.definition;

import lombok.Data;

/**
 * 单个方法参数的输入配置。source 与 defaultValue 二选一，运行时不做类型转换。
 */
@Data
public class ParamInput {

    /**
     * 方法参数索引，从 0 开始
     */
    private Integer paramIndex;

    /**
     * 数据来源，例如 input.raw_message、n1.result
     */
    private String source;

    /**
     * 默认值，JSON 原生值
     */
    private Object defaultValue;
}
