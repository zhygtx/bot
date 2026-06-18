package com.example.demo.pojo.entity.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 工作流日志类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkflowLog {

    /**
     * 工作流日志 ID（自增主键）
     */
    private Long id;

    /**
     * 工作流 ID
     */
    private String workflowId;

    /**
     * 用户 ID
     */
    private String userId;

    /**
     * 工作流预计执行节点个数
     */
    private Integer expectedNodeCount;

    /**
     * 工作流实际执行节点个数
     */
    private Integer actualNodeCount;

    /**
     * 工作流执行耗时
     */
    private Long executionTime;

    /**
     * 工作流执行开始时间
     */
    private Long startTime;

    /**
     * 工作流初始上下文
     */
    private String initialContext;

    /**
     * 工作流名称
     */
    private String workflowName;

    /**
     * 工作流报错日志
     */
    private String errorLog;

    /**
     * 工作流是否报错
     */
    private Boolean isError;

    /**
     * 工作流节点日志列表
     */
    private List<NodeLog> nodeLogs;
}