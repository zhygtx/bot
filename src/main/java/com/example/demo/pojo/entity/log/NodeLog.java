package com.example.demo.pojo.entity.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 节点日志类
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class NodeLog {

    /**
     * 节点日志 ID（自增主键）
     */
    private Long id;

    /**
     * 工作流日志 ID
     */
    private Long workflowLogId;

    /**
     * 节点 ID
     */
    private String nodeId;

    /**
     * 节点使用的插件方法ID
     */
    private String methodId;

    /**
     * 节点执行耗时
     */
    private Long executionTime;

    /**
     * 节点执行次序
     */
    private Integer order;

    /**
     * 节点输入内容
     */
    private String input;

    /**
     * 节点输出内容
     */
    private String output;

    /**
     * 节点方法名称
     */
    private String methodName;

    /**
     * 节点方法描述
     */
    private String methodDescription;
}