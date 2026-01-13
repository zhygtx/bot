package com.example.demo.controller.task;

import com.example.demo.pojo.Result;
import com.example.demo.pojo.task.Role;
import com.example.demo.service.task.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/role")
public class RoleController {

    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * 获取所有任务触发规则，按作用域分组
     * @return 任务触发规则映射
     */
    @GetMapping("/grouped")
    public Result<Map<String, List<Role>>> getAllRolesGrouped() {
        return Result.success(roleService.getAllRoles());
    }

    /**
     * 获取所有任务触发规则列表
     * @return 任务触发规则列表
     */
    @GetMapping
    public Result<List<Role>> getAllRoles() {
        return Result.success(roleService.getAllRolesList());
    }

    /**
     * 根据ID获取任务触发规则
     * @param id 任务触发规则ID
     * @return 任务触发规则
     */
    @GetMapping("/{id}")
    public Result<Role> getRoleById(@PathVariable String id) {
        Role role = roleService.getRoleById(id);
        if (role == null) {
            return Result.error("任务触发规则不存在");
        }
        return Result.success(role);
    }

    /**
     * 根据用户ID获取任务触发规则列表
     * @param userId 用户ID
     * @return 任务触发规则列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<Role>> getRolesByUserId(@PathVariable String userId) {
        return Result.success(roleService.getRolesByUserId(userId));
    }

    /**
     * 根据作用域ID获取任务触发规则列表
     * @param scopeId 作用域ID
     * @return 任务触发规则列表
     */
    @GetMapping("/scope/{scopeId}")
    public Result<List<Role>> getRolesByScopeId(@PathVariable String scopeId) {
        return Result.success(roleService.getRolesByScopeId(scopeId));
    }

    /**
     * 创建任务触发规则
     * @param role 任务触发规则
     * @return 任务触发规则
     */
    @PostMapping
    public Result<Role> addRole(@RequestBody Role role) {
        Role addedRole = roleService.addRole(role);
        return Result.success("任务触发规则添加成功", addedRole);
    }

    /**
     * 更新任务触发规则
     * @param id 任务触发规则ID
     * @param role 任务触发规则
     * @return 任务触发规则
     */
    @PutMapping("/{id}")
    public Result<Role> updateRole(@PathVariable String id, @RequestBody Role role) {
        role.setId(id);
        Role updatedRole = roleService.updateRole(role);
        return Result.success("任务触发规则更新成功", updatedRole);
    }

    /**
     * 删除任务触发规则
     * @param id 任务触发规则ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteRoleById(@PathVariable String id) {
        int result = roleService.deleteRoleById(id);
        if (result > 0) {
            return Result.success("任务触发规则删除成功");
        } else {
            return Result.error("任务触发规则删除失败");
        }
    }

    /**
     * 根据作用域ID删除任务触发规则
     * @param scopeId 作用域ID
     * @return 删除结果
     */
    @DeleteMapping("/scope/{scopeId}")
    public Result<String> deleteRolesByScopeId(@PathVariable String scopeId) {
        int result = roleService.deleteRolesByScopeId(scopeId);
        return Result.success("成功删除" + result + "条任务触发规则");
    }

}