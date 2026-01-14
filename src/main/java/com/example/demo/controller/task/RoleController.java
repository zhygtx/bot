package com.example.demo.controller.task;

import com.example.demo.pojo.Result;
import com.example.demo.pojo.task.Role;
import com.example.demo.service.task.RoleService;
import com.example.demo.utils.AuthUtil;
import com.example.demo.utils.MD5Util;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.NoSuchAlgorithmException;
import java.util.List;

@RestController
@RequestMapping("/api/role")
public class RoleController {

    private final RoleService roleService;
    private final AuthUtil authUtil;

    @Autowired
    public RoleController(RoleService roleService, AuthUtil authUtil) {
        this.roleService = roleService;
        this.authUtil = authUtil;
    }

    /**
     * 获取当前用户的所有任务触发规则列表
     * @param request HTTP请求
     * @return 任务触发规则列表
     */
    @RequestMapping("/list")
    public Result<List<Role>> getRolesByCurrentUser(HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        List<Role> roles = roleService.getRolesByUserId(userId);
        return Result.success(roles);
    }

    /**
     * 根据ID获取任务触发规则
     * @param id 任务触发规则ID
     * @param request HTTP请求
     * @return 任务触发规则
     */
    @RequestMapping("/{id}")
    public Result<Role> getRoleById(@PathVariable String id, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        Role role = roleService.getRoleById(id);
        if (role == null) {
            return Result.error("任务触发规则不存在");
        }
        
        // 验证权限，确保用户只能访问自己的任务触发规则
        if (!userId.equals(role.getUserId())) {
            return Result.error("无权限访问该任务触发规则");
        }
        
        return Result.success(role);
    }

    /**
     * 根据作用域ID获取任务触发规则列表
     * @param scopeId 作用域ID
     * @param request HTTP请求
     * @return 任务触发规则列表
     */
    @RequestMapping("/scope/{scopeId}")
    public Result<List<Role>> getRolesByScopeId(@PathVariable String scopeId, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        List<Role> roles = roleService.getRolesByScopeId(scopeId);
        // 过滤出当前用户的任务触发规则
        roles.removeIf(role -> !userId.equals(role.getUserId()));
        return Result.success(roles);
    }

    /**
     * 创建任务触发规则
     * @param role 任务触发规则
     * @param request HTTP请求
     * @return 任务触发规则
     */
    @RequestMapping("/add")
    public Result<Role> addRole(@RequestBody Role role, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        // 设置当前用户ID
        role.setUserId(userId);
        String md5;
        try {
            md5 = MD5Util.calculateClassMD5(role);
        } catch (NoSuchAlgorithmException e) {
            return Result.error("MD5计算出现问题");
        }
        if (roleService.existsByMd5(md5)){
            return Result.error("该触发规则已存在");
        }
        role.setMD5(md5);
        Role addedRole = roleService.addRole(role);
        return Result.success(addedRole);
    }

    /**
     * 更新任务触发规则
     * @param id 任务触发规则ID
     * @param role 任务触发规则
     * @param request HTTP请求
     * @return 任务触发规则
     */
    @RequestMapping("/update/{id}")
    public Result<Role> updateRole(@PathVariable String id, @RequestBody Role role, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        // 验证权限，确保用户只能更新自己的任务触发规则
        Role existingRole = roleService.getRoleById(id);
        if (existingRole == null) {
            return Result.error("任务触发规则不存在");
        }
        if (!userId.equals(existingRole.getUserId())) {
            return Result.error("无权限更新该任务触发规则");
        }
        
        // 设置当前用户ID和ID
        role.setUserId(userId);
        role.setId(id);
        String md5;
        try {
            md5 = MD5Util.calculateClassMD5(role);
        } catch (NoSuchAlgorithmException e) {
            return Result.error("MD5计算出现问题");
        }
        if (roleService.existsByMd5(md5)){
            return Result.error("该触发规则已存在");
        }
        role.setMD5(md5);
        Role updatedRole = roleService.updateRole(role);
        return Result.success("任务触发规则更新成功", updatedRole);
    }

    /**
     * 删除任务触发规则
     * @param id 任务触发规则ID
     * @param request HTTP请求
     * @return 删除结果
     */
    @RequestMapping("/delete/{id}")
    public Result<String> deleteRoleById(@PathVariable String id, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        // 验证权限，确保用户只能删除自己的任务触发规则
        Role existingRole = roleService.getRoleById(id);
        if (existingRole == null) {
            return Result.error("任务触发规则不存在");
        }
        if (!userId.equals(existingRole.getUserId())) {
            return Result.error("无权限删除该任务触发规则");
        }
        
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
     * @param request HTTP请求
     * @return 删除结果
     */
    @RequestMapping("/delete/scope/{scopeId}")
    public Result<String> deleteRolesByScopeId(@PathVariable String scopeId, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        // 验证权限，确保用户只能删除自己的任务触发规则
        List<Role> roles = roleService.getRolesByScopeId(scopeId);
        for (Role role : roles) {
            if (!userId.equals(role.getUserId())) {
                return Result.error("无权限删除该作用域下的任务触发规则");
            }
        }
        
        int result = roleService.deleteRolesByScopeId(scopeId);
        return Result.success("成功删除" + result + "条任务触发规则");
    }

}