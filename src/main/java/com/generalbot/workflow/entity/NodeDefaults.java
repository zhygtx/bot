package com.generalbot.workflow.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NodeDefaults {

    /**
     * 默认值ID
     */
    private String id;

    /**
     * 节点ID
     */
    private String nodeId;

    /**
     * 方法的第几个参数
     */
    private Integer paramIndex;

    /**
     * 方法参数名称
     */
    private String paramName;

    /**
     * 字段名(支持嵌套如：user.id，无嵌套直接映射时为user即和参数名相同)
     */
    private String fieldPath;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 默认值类型
     */
    private DefaultValueType defaultValueType;

    /**
     * 默认值类型枚举(基本数据类型，字符串，数字，布尔值)
     */
    public enum DefaultValueType {
        String,
        Integer,
        Double,
        Boolean,
        Long
    }
}
