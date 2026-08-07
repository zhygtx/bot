package com.generalbot.workflow.entity.execution;

import lombok.Data;

/**
 * 工作流执行记录。每次执行一行，trace 内嵌节点明细。
 */
@Data
public class WorkflowExecution {

    /**
     * 执行记录ID
     */
    private Long id;

    /**
     * 工作流ID
     */
    private String workflowId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 触发键
     */
    private String triggerKey;

    /**
     * 工作流名称（冗余，便于列表展示）
     */
    private String workflowName;

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
     * 执行耗时（毫秒）
     */
    private Long durationMs;

    /**
     * 预期节点数
     */
    private Integer expectedNodeCount;

    /**
     * 实际执行节点数
     */
    private Integer actualNodeCount;

    /**
     * 工作流级错误信息
     */
    private String errorMessage;

    /**
     * 节点执行轨迹
     */
    private ExecutionTrace trace;
}
