package com.example.demo.controller;

import com.example.demo.pojo.entity.Result;
import com.example.demo.service.WorkflowLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
                                      @RequestParam(required = false,defaultValue = "1") Integer pageNum,
                                      @RequestParam(required = false,defaultValue = "10") Integer pageSize) {
        return Result.success(null,workflowLogService.findWorkflowLogs(userId, workflowName, startTime, endTime, sortField, sortOrder, pageNum, pageSize));
    }

    /**
     * 查询工作流节点日志
     * @param workflowLogId 工作流日志ID
     * @return 工作流节点日志列表
     */
    @GetMapping("/findNodeLogs")
    public Result<?> findNodeLogs(String workflowLogId) {
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
}
