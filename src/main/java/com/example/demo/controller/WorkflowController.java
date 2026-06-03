package com.example.demo.controller;

import com.example.demo.pojo.entity.Result;
import com.example.demo.pojo.entity.workflow.WorkflowInfo;
import com.example.demo.service.WorkflowService;
import com.example.demo.util.AuthUtil;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 工作流控制器
 */
@Slf4j
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
        String authorName = authUtil.getCurrentUserName(request);
        workflowInfo.setId(UUID.randomUUID().toString());
        workflowInfo.setUserId(userId);
        workflowInfo.setAuthorName(authorName);
        int result = workflowService.add(workflowInfo);
        return result > 0 ? Result.success(null, workflowService.findById(workflowInfo.getId())) : Result.error(500, "添加工作流失败");
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

    /**
     * 修改工作流
     * @param workflowInfo 工作流信息
     * @return 修改结果
     */
    @PutMapping
    public Result<?> edit(@RequestBody WorkflowInfo workflowInfo) {
        int result = workflowService.edit(workflowInfo);
        return result > 0 ? Result.success(null, workflowService.findById(workflowInfo.getId())) : Result.error(500, "修改工作流失败");
    }

    /**
     * 查询所有工作流
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 工作流列表
     */
    @GetMapping("/findAll")
    public Result<?> findByAuthorId(HttpServletRequest request,
            @RequestParam(required = false,defaultValue = "1") int pageNum,
            @RequestParam(required = false,defaultValue = "12") int pageSize) {
        String userId = authUtil.getCurrentUserId(request);
        return Result.success(null,workflowService.findAll(userId,pageNum, pageSize));
    }

    /**
     * 查询工作流
     * @param id 工作流ID
     * @return 工作流信息
     */
    @GetMapping("{id}")
    public Result<?> findById(@PathVariable("id") String id) {
        return Result.success(null,workflowService.findById(id));
    }

    /**
     * 测试工作流
     * @param workflowId 工作流ID
     * @return 测试结果
     */
    @PostMapping("/test")
    public Result<?> test(String workflowId) {
        try {
            JsonNode test = workflowService.test(workflowService.findById(workflowId));
            return Result.success(null, test);
        } catch (Exception e) {
            log.error("测试工作流失败：{}", e.getMessage());
            return Result.error(500, "测试工作流失败：" + e.getMessage());
        }
    }
}
