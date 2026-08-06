package com.generalbot.workflow.controller;

import com.generalbot.common.api.Result;
import com.generalbot.workflow.entity.WorkflowInfo;
import com.generalbot.security.UserPrincipal;
import com.generalbot.workflow.service.WorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    /**
     * 添加工作流
     * @param user 用户信息
     * @param workflowInfo 工作流信息
     * @return 添加结果
     */
    @PostMapping
    public Result<?> add(@AuthenticationPrincipal UserPrincipal user, @RequestBody WorkflowInfo workflowInfo) {
        String userId = user.userId();
        workflowInfo.setId(UUID.randomUUID().toString());
        workflowInfo.setUserId(userId);
        int result = workflowService.add(workflowInfo);
        return result > 0 ? Result.success("创建成功", workflowService.findById(workflowInfo.getId())) : Result.error(500, "添加工作流失败");
    }

    /**
     * 删除工作流
     * @param id 工作流ID
     * @return 删除结果
     */
    @DeleteMapping
    public Result<?> remove(String id) {
        int result = workflowService.remove(id);
        return result > 0 ? Result.success("删除成功", null) : Result.error(500, "删除工作流失败");
    }

    /**
     * 修改工作流
     * @param workflowInfo 工作流信息
     * @return 修改结果
     */
    @PutMapping
    public Result<?> edit(@RequestBody WorkflowInfo workflowInfo) {
        int result = workflowService.edit(workflowInfo);
        return result > 0 ? Result.success("修改成功", workflowService.findById(workflowInfo.getId())) : Result.error(500, "修改工作流失败");
    }

    /**
     * 修改工作流启用状态
     * @param id 工作流ID
     * @param enabled 启用状态
     * @return 修改结果
     */
    @PutMapping("/editEnabled")
    public Result<?> editEnabled(String id, boolean enabled) {
        int result = workflowService.editEnabled(id, enabled);
        return result > 0 ? Result.success("修改成功",null) : Result.error(500, "修改工作流启用状态失败");
    }

    /**
     * 查询所有工作流
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 工作流列表
     */
    @GetMapping("/findAll")
    public Result<?> findByAuthorId(@AuthenticationPrincipal UserPrincipal user,
            @RequestParam(required = false,defaultValue = "1") int pageNum,
            @RequestParam(required = false,defaultValue = "12") int pageSize) {
        String userId = user.userId();
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
            Long logId = workflowService.test(workflowService.findById(workflowId));
            return Result.success(null, logId);
        } catch (Exception e) {
            log.error("测试工作流失败：{}", e.getMessage());
            return Result.error(500, "测试工作流失败：" + e.getMessage());
        }
    }
}
