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
     * @param keyword 内容关键词，模糊匹配执行 trace 中的节点数据
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
                                      String keyword,
                                      @RequestParam(defaultValue = "1") Integer pageNum,
                                      @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(null, executionService.findExecutions(
                userId, workflowId, workflowName, startTime, endTime, sortField, sortOrder, status, keyword, pageNum, pageSize));
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

    /**
     * 查询离线存储的大数据内容
     * @param key 大数据引用键（BIG_TEXT: 前缀）
     * @return 大数据内容
     */
    @GetMapping("/findBigText")
    public Result<?> findBigText(String key) {
        return Result.success(null, executionService.findBigText(key));
    }
}
