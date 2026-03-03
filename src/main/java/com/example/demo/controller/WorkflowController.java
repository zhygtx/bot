package com.example.demo.controller;

import com.example.demo.pojo.Result;
import com.example.demo.pojo.workflow.WorkflowInfo;
import com.example.demo.service.WorkflowService;
import com.example.demo.util.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

/**
 * 工作流控制器
 */
@RestController
@RequestMapping("/workflow")
public class WorkflowController {

    private final WorkflowService workflowService;
    private final AuthUtil authUtil;

    public WorkflowController(WorkflowService workflowService, AuthUtil authUtil) {
        this.workflowService = workflowService;
        this.authUtil = authUtil;
    }

    /**
     * 添加工作流
     * @param request HTTP请求
     * @param workflowInfo 工作流信息
     * @return 添加结果
     */
    @PostMapping
    public Result<?> add(HttpServletRequest request, @RequestBody WorkflowInfo workflowInfo) {
        String userId = authUtil.getCurrentUserId(request);
        workflowInfo.setUserId(userId);
        int result = workflowService.add(workflowInfo);
        return result > 0 ? Result.success(null, null) : Result.error(500, "添加工作流失败");
    }

    /**
     * 删除工作流
     * @param id 工作流ID
     * @return 删除结果
     */
    @DeleteMapping
    public Result<?> remove(String id) {
        int result = workflowService.remove(id);
        return result > 0 ? Result.success(null, null) : Result.error(500, "删除工作流失败");
    }
}
