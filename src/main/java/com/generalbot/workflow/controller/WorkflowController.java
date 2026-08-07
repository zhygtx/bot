package com.generalbot.workflow.controller;

import com.generalbot.common.api.Result;
import com.generalbot.security.UserPrincipal;
import com.generalbot.workflow.engine.CallableRegistry;
import com.generalbot.workflow.engine.CallableDescriptor;
import com.generalbot.workflow.entity.WorkflowInfo;
import com.generalbot.workflow.service.WorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作流控制器。
 */
@Slf4j
@RestController
@RequestMapping("/workflow")
public class WorkflowController {

    private final WorkflowService workflowService;
    private final CallableRegistry callableRegistry;

    public WorkflowController(WorkflowService workflowService, CallableRegistry callableRegistry) {
        this.workflowService = workflowService;
        this.callableRegistry = callableRegistry;
    }

    /**
     * 新增工作流
     * @param user 当前用户
     * @param workflowInfo 工作流信息
     * @return 保存后的工作流
     */
    @PostMapping
    public Result<?> add(@AuthenticationPrincipal UserPrincipal user, @RequestBody WorkflowInfo workflowInfo) {
        workflowInfo.setUserId(user.userId());
        return Result.success("创建成功", workflowService.add(workflowInfo));
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
        return result > 0 ? Result.success("修改成功", workflowService.findById(workflowInfo.getId()))
                : Result.error(500, "修改工作流失败");
    }

    /**
     * 修改启用状态
     * @param id 工作流ID
     * @param enabled 是否启用
     * @return 修改结果
     */
    @PutMapping("/editEnabled")
    public Result<?> editEnabled(String id, boolean enabled) {
        int result = workflowService.editEnabled(id, enabled);
        return result > 0 ? Result.success("修改成功", null) : Result.error(500, "修改工作流启用状态失败");
    }

    /**
     * 分页查询工作流
     * @param user 当前用户
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 工作流列表
     */
    @GetMapping("/findAll")
    public Result<?> findAll(@AuthenticationPrincipal UserPrincipal user,
                             @RequestParam(defaultValue = "1") int pageNum,
                             @RequestParam(defaultValue = "12") int pageSize) {
        return Result.success(null, workflowService.findAll(user.userId(), pageNum, pageSize));
    }

    /**
     * 根据ID查询工作流
     * @param id 工作流ID
     * @return 工作流信息
     */
    @GetMapping("{id}")
    public Result<?> findById(@PathVariable("id") String id) {
        return Result.success(null, workflowService.findById(id));
    }

    /**
     * 测试工作流
     * @param workflowId 工作流ID
     * @return 执行记录ID
     */
    @PostMapping("/test")
    public Result<?> test(String workflowId) {
        try {
            return Result.success(null, workflowService.test(workflowId));
        } catch (Exception e) {
            log.error("测试工作流失败：{}", e.getMessage());
            return Result.error(500, "测试工作流失败：" + e.getMessage());
        }
    }

    /**
     * 批量解析 callable key，返回描述信息。
     * @param keys callable key 列表
     * @return key -> 描述
     */
    @PostMapping("/resolveCallables")
    public Result<?> resolveCallables(@RequestBody List<String> keys) {
        Map<String, CallableDescriptor> result = new LinkedHashMap<>();
        for (String key : keys) {
            result.put(key, callableRegistry.describe(key));
        }
        return Result.success(null, result);
    }
}
