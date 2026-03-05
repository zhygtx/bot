package com.example.demo.pojo.workflow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 增强版数据映射类 - 支持复杂映射场景
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataMap {

    /**
     * 映射关系ID
     */
    private String id;

    /**
     * 所属节点ID
     */
    private String nodeId;

    /**
     * 源数据所属节点
     */
    private String sourceNodeId;

    /**
     * 源数据字段名称(支持嵌套如：value.id，无嵌套直接映射时为value)
     */
    private String sourcePath;

    /**
     * 目标参数名称
     */
    private String targetParamName;

    /**
     * 方法的第几个参数
     */
    private Integer paramIndex;

    /**
     * 目标参数字段名(支持嵌套如：user.id，无嵌套直接映射时为user即和参数名相同)
     */
    private String targetPath;

    /**
     * 源数据字段类型
     */
    private String sourceType;

    /**
     * 目标属性方法参数字段类型
     */
    private String targetType;
}