package com.example.demo.controller.task;

import com.example.demo.pojo.Result;
import com.example.demo.pojo.task.Scope;
import com.example.demo.service.task.ScopeService;
import com.example.demo.utils.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scope")
public class ScopeController {

    private final ScopeService scopeService;
    private final AuthUtil authUtil;

    @Autowired
    public ScopeController(ScopeService scopeService, AuthUtil authUtil) {
        this.scopeService = scopeService;
        this.authUtil = authUtil;
    }

    /**
     * 获取当前用户的所有作用域
     * @param request HTTP请求
     * @return 作用域列表
     */
    @RequestMapping("/list")
    public Result<List<Scope>> getScopesByCurrentUser(HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        List<Scope> scopes = scopeService.getScopesByUserId(userId);
        return Result.success(scopes);
    }

    /**
     * 根据ID获取作用域
     * @param id 作用域ID
     * @param request HTTP请求
     * @return 作用域
     */
    @RequestMapping("/{id}")
    public Result<Scope> getScopeById(@PathVariable String id, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        Scope scope = scopeService.getScopeById(id);
        if (scope == null) {
            return Result.error("作用域不存在");
        }
        
        // 验证权限，确保用户只能访问自己的作用域
        if (!userId.equals(scope.getUserId())) {
            return Result.error("无权限访问该作用域");
        }
        
        return Result.success(scope);
    }

    /**
     * 创建作用域
     * @param scope 作用域
     * @param request HTTP请求
     * @return 作用域
     */
    @RequestMapping("/add")
    public Result<Scope> addScope(@RequestBody Scope scope, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        // 设置当前用户ID
        scope.setUserId(userId);
        Scope addedScope = scopeService.addScope(scope);
        return Result.success("作用域添加成功", addedScope);
    }

    /**
     * 更新作用域
     * @param id 作用域ID
     * @param scope 作用域
     * @param request HTTP请求
     * @return 作用域
     */
    @RequestMapping("/update/{id}")
    public Result<Scope> updateScope(@PathVariable String id, @RequestBody Scope scope, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        // 验证权限，确保用户只能更新自己的作用域
        Scope existingScope = scopeService.getScopeById(id);
        if (existingScope == null) {
            return Result.error("作用域不存在");
        }
        if (!userId.equals(existingScope.getUserId())) {
            return Result.error("无权限更新该作用域");
        }
        
        // 设置当前用户ID和ID
        scope.setUserId(userId);
        scope.setId(id);
        Scope updatedScope = scopeService.updateScope(scope);
        return Result.success("作用域更新成功", updatedScope);
    }

    /**
     * 删除作用域
     * @param id 作用域ID
     * @param request HTTP请求
     * @return 删除结果
     */
    @RequestMapping("/delete/{id}")
    public Result<String> deleteScopeById(@PathVariable String id, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        // 验证权限，确保用户只能删除自己的作用域
        Scope existingScope = scopeService.getScopeById(id);
        if (existingScope == null) {
            return Result.error("作用域不存在");
        }
        if (!userId.equals(existingScope.getUserId())) {
            return Result.error("无权限删除该作用域");
        }
        
        int result = scopeService.deleteScopeById(id);
        if (result > 0) {
            return Result.success("作用域删除成功");
        } else {
            return Result.error("作用域删除失败");
        }
    }

}