package com.example.demo.controller;

import com.example.demo.pojo.Result;
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
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 工作流日志列表
     */
    @GetMapping("/findWorkflowLogs")
    public Result<?> findWorkflowLogs(String userId,
                                      @RequestParam(required = false,defaultValue = "1") Integer pageNum,
                                      @RequestParam(required = false,defaultValue = "10") Integer pageSize) {
        return Result.success(null,workflowLogService.findWorkflowLogs(userId, pageNum, pageSize));
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
}
