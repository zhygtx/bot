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
     * 源数据字段名称
     */
    private String source;

    /**
     * 目标方法参数名称
     */
    private String target;

    /**
     * 源数据字段类型
     */
    private String sourceType;

    /**
     * 目标方法参数类型
     */
    private String targetType;
}