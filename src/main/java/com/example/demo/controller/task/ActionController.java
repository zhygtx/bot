package com.example.demo.controller.task;

import com.example.demo.pojo.Result;
import com.example.demo.pojo.task.Action;
import com.example.demo.service.task.ActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/action")
public class ActionController {

    private final ActionService actionService;

    @Autowired
    public ActionController(ActionService actionService) {
        this.actionService = actionService;
    }

    /**
     * 根据规则ID获取动作列表
     * @param roleId 规则ID
     * @return 动作列表
     */
    @GetMapping("/role/{roleId}")
    public Result<List<Action>> getActionsByRoleId(@PathVariable String roleId) {
        return Result.success(actionService.getActions(roleId));
    }

    /**
     * 根据ID获取动作
     * @param id 动作ID
     * @return 动作
     */
    @GetMapping("/{id}")
    public Result<Action> getActionById(@PathVariable String id) {
        Action action = actionService.getActionById(id);
        if (action == null) {
            return Result.error("动作不存在");
        }
        return Result.success(action);
    }

    /**
     * 根据用户ID获取动作列表
     * @param userId 用户ID
     * @return 动作列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<Action>> getActionsByUserId(@PathVariable String userId) {
        return Result.success(actionService.getActionsByUserId(userId));
    }

    /**
     * 创建动作
     * @param action 动作
     * @return 动作
     */
    @PostMapping
    public Result<Action> addAction(@RequestBody Action action) {
        Action addedAction = actionService.addAction(action);
        return Result.success("动作添加成功", addedAction);
    }

    /**
     * 更新动作
     * @param id 动作ID
     * @param action 动作
     * @return 动作
     */
    @PutMapping("/{id}")
    public Result<Action> updateAction(@PathVariable String id, @RequestBody Action action) {
        action.setId(id);
        Action updatedAction = actionService.updateAction(action);
        return Result.success("动作更新成功", updatedAction);
    }

    /**
     * 删除动作
     * @param id 动作ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteActionById(@PathVariable String id) {
        int result = actionService.deleteActionById(id);
        if (result > 0) {
            return Result.success("动作删除成功");
        } else {
            return Result.error("动作删除失败");
        }
    }

    /**
     * 根据规则ID删除动作
     * @param roleId 规则ID
     * @return 删除结果
     */
    @DeleteMapping("/role/{roleId}")
    public Result<String> deleteActionsByRoleId(@PathVariable String roleId) {
        int result = actionService.deleteActionsByRoleId(roleId);
        return Result.success("成功删除" + result + "条动作");
    }

}