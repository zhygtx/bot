package com.generalbot.workflow.entity.execution;

import lombok.Data;

/**
 * 单个节点的执行快照，节点ID与 callable 名称都保存，历史日志不受后续编辑影响。
 */
@Data
public class NodeTrace {

    /**
     * 节点ID
     */
    private String nodeId;

    /**
     * callable 引用
     */
    private String callable;

    /**
     * 节点显示名称
     */
    private String name;

    /**
     * 状态：SUCCESS/FAILED
     */
    private String status;

    /**
     * 开始时间戳
     */
    private Long startTime;

    /**
     * 结束时间戳
     */
    private Long endTime;

    /**
     * 节点输入
     */
    private Object input;

    /**
     * 节点输出
     */
    private Object output;

    /**
     * 错误信息
     */
    private String error;
}
