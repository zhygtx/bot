package com.example.demo.controller.task;

import com.example.demo.pojo.Result;
import com.example.demo.pojo.task.Action;
import com.example.demo.service.task.ActionService;
import com.example.demo.utils.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/action")
public class ActionController {

    private final ActionService actionService;
    private final AuthUtil authUtil;

    @Autowired
    public ActionController(ActionService actionService, AuthUtil authUtil) {
        this.actionService = actionService;
        this.authUtil = authUtil;
    }

    /**
     * 根据规则ID获取动作列表
     * @param roleId 规则ID
     * @param request HTTP请求
     * @return 动作列表
     */
    @RequestMapping("/role/{roleId}")
    public Result<List<Action>> getActionsByRoleId(@PathVariable String roleId, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        List<Action> actions = actionService.getActions(roleId);
        // 过滤出当前用户的动作
        actions.removeIf(action -> !userId.equals(action.getUserId()));
        return Result.success(actions);
    }

    /**
     * 根据ID获取动作
     * @param id 动作ID
     * @param request HTTP请求
     * @return 动作
     */
    @RequestMapping("/{id}")
    public Result<Action> getActionById(@PathVariable String id, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        Action action = actionService.getActionById(id);
        if (action == null) {
            return Result.error("动作不存在");
        }
        
        // 验证权限，确保用户只能访问自己的动作
        if (!userId.equals(action.getUserId())) {
            return Result.error("无权限访问该动作");
        }
        
        return Result.success(action);
    }

    /**
     * 获取当前用户的所有动作列表
     * @param request HTTP请求
     * @return 动作列表
     */
    @RequestMapping("/list")
    public Result<List<Action>> getActionsByCurrentUser(HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        List<Action> actions = actionService.getActionsByUserId(userId);
        return Result.success(actions);
    }

    /**
     * 创建动作
     * @param action 动作
     * @param request HTTP请求
     * @return 动作
     */
    @RequestMapping("/add")
    public Result<Action> addAction(@RequestBody Action action, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        // 设置当前用户ID
        action.setUserId(userId);
        Action addedAction = actionService.addAction(action);
        return Result.success("动作添加成功", addedAction);
    }

    /**
     * 更新动作
     * @param id 动作ID
     * @param action 动作
     * @param request HTTP请求
     * @return 动作
     */
    @RequestMapping("/update/{id}")
    public Result<Action> updateAction(@PathVariable String id, @RequestBody Action action, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        // 验证权限，确保用户只能更新自己的动作
        Action existingAction = actionService.getActionById(id);
        if (existingAction == null) {
            return Result.error("动作不存在");
        }
        if (!userId.equals(existingAction.getUserId())) {
            return Result.error("无权限更新该动作");
        }
        
        // 设置当前用户ID和ID
        action.setUserId(userId);
        action.setId(id);
        Action updatedAction = actionService.updateAction(action);
        return Result.success("动作更新成功", updatedAction);
    }

    /**
     * 删除动作
     * @param id 动作ID
     * @param request HTTP请求
     * @return 删除结果
     */
    @RequestMapping("/delete/{id}")
    public Result<String> deleteActionById(@PathVariable String id, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        // 验证权限，确保用户只能删除自己的动作
        Action existingAction = actionService.getActionById(id);
        if (existingAction == null) {
            return Result.error("动作不存在");
        }
        if (!userId.equals(existingAction.getUserId())) {
            return Result.error("无权限删除该动作");
        }
        
        actionService.deleteActionById(id);
        return  Result.success();
    }

    /**
     * 根据规则ID删除动作
     * @param roleId 规则ID
     * @param request HTTP请求
     * @return 删除结果
     */
    @RequestMapping("/delete/role/{roleId}")
    public Result<String> deleteActionsByRoleId(@PathVariable String roleId, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        // 验证权限，确保用户只能删除自己的动作
        List<Action> actions = actionService.getActions(roleId);
        for (Action action : actions) {
            if (!userId.equals(action.getUserId())) {
                return Result.error("无权限删除该规则下的动作");
            }
        }
        
        int result = actionService.deleteActionsByRoleId(roleId);
        return Result.success("成功删除" + result + "条动作");
    }

}