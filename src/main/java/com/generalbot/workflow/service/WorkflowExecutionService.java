package com.generalbot.workflow.service;

import com.generalbot.workflow.entity.execution.WorkflowExecution;
import com.github.pagehelper.PageInfo;

/**
 * 工作流执行记录服务。
 */
public interface WorkflowExecutionService {

    /**
     * 分页查询执行记录
     * @param userId 用户ID
     * @param workflowId 工作流ID
     * @param workflowName 工作流名称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param sortField 排序字段
     * @param sortOrder 排序方式
     * @param status 状态
     * @param keyword 内容关键词，模糊匹配执行 trace 中的节点数据
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    PageInfo<WorkflowExecution> findExecutions(String userId,
                                               String workflowId,
                                               String workflowName,
                                               Long startTime,
                                               Long endTime,
                                               String sortField,
                                               String sortOrder,
                                               String status,
                                               String keyword,
                                               int pageNum,
                                               int pageSize);

    /**
     * 根据ID查询执行记录
     * @param id 执行记录ID
     * @return 执行记录
     */
    WorkflowExecution findById(Long id);
}
