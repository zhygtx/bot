package com.generalbot.workflow.controller;

import com.generalbot.common.api.Result;
import com.generalbot.workflow.entity.execution.WorkflowExecution;
import com.generalbot.workflow.service.WorkflowExecutionService;
import org.springframework.web.bind.annotation.*;

/**
 * 工作流执行记录控制器。
 */
@RestController
@RequestMapping("/workflowLog")
public class WorkflowLogController {

    private final WorkflowExecutionService executionService;

    public WorkflowLogController(WorkflowExecutionService executionService) {
        this.executionService = executionService;
    }

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
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 执行记录分页
     */
    @GetMapping("/findWorkflowLogs")
    public Result<?> findWorkflowLogs(String userId,
                                      String workflowId,
                                      String workflowName,
                                      Long startTime,
                                      Long endTime,
                                      String sortField,
                                      String sortOrder,
                                      String status,
                                      @RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(null, executionService.findExecutions(
                userId, workflowId, workflowName, startTime, endTime, sortField, sortOrder, status, pageNum, pageSize));
    }

    /**
     * 根据ID查询执行记录
     * @param id 执行记录ID
     * @return 执行记录
     */
    @GetMapping("{id}")
    public Result<?> getWorkflowLogById(@PathVariable("id") Long id) {
        WorkflowExecution execution = executionService.findById(id);
        return Result.success(null, execution);
    }
}
