package com.example.demo.controller;

import com.example.demo.pojo.entity.Result;
import com.example.demo.service.WorkflowLogService;
import org.springframework.web.bind.annotation.*;

/**
 * 工作流日志控制器
 */
@RestController
@RequestMapping("/workflowLog")
public class WorkflowLogController {

    private final WorkflowLogService workflowLogService;

    public WorkflowLogController(WorkflowLogService workflowLogService) {
        this.workflowLogService = workflowLogService;
    }

    /**
     * 查询工作流日志
     * @param userId 用户ID
     * @param workflowName 工作流名称（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param sortField 排序字段（可选：actualNodeCount, executionTime）
     * @param sortOrder 排序方式（可选：asc, desc）
     * @param status 执行状态（可选：success-成功, failed-失败）
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 工作流日志列表
     */
    @GetMapping("/findWorkflowLogs")
    public Result<?> findWorkflowLogs(String userId,
                                      String workflowName,
                                      Long startTime,
                                      Long endTime,
                                      String sortField,
                                      String sortOrder,
                                      String status,
                                      @RequestParam(required = false,defaultValue = "1") Integer pageNum,
                                      @RequestParam(required = false,defaultValue = "10") Integer pageSize) {
        return Result.success(null,workflowLogService.findWorkflowLogs(userId, workflowName, startTime, endTime, sortField, sortOrder, status, pageNum, pageSize));
    }

    /**
     * 查询工作流节点日志
     * @param workflowLogId 工作流日志ID
     * @return 工作流节点日志列表
     */
    @GetMapping("/findNodeLogs")
    public Result<?> findNodeLogs(Long workflowLogId) {
        return Result.success(null,workflowLogService.findNodeLogs(workflowLogId));
    }

    /**
     * 查询大数据内容
     * @param key 大数据引用键
     * @return 大数据内容
     */
    @GetMapping("/findBigText")
    public Result<?> findBigText(String key) {
        return Result.success(null, workflowLogService.findBigText(key));
    }

    /**
     * 根据ID查询工作流日志
     * @param id 工作流日志ID
     * @return 工作流日志
     */
    @GetMapping("{id}")
    public Result<?> getWorkflowLogById(@PathVariable("id") Long id) {
        return Result.success(null, workflowLogService.findWorkflowLogById(id));
    }
}