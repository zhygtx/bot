package com.example.demo.controller.task;

import com.example.demo.pojo.Result;
import com.example.demo.pojo.task.Scope;
import com.example.demo.service.task.ScopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scope")
public class ScopeController {

    private final ScopeService scopeService;

    @Autowired
    public ScopeController(ScopeService scopeService) {
        this.scopeService = scopeService;
    }

    /**
     * 获取所有作用域
     * @return 作用域列表
     */
    @GetMapping
    public Result<List<Scope>> getAllScopes() {
        return Result.success(scopeService.getAllScopes());
    }

    /**
     * 根据ID获取作用域
     * @param id 作用域ID
     * @return 作用域
     */
    @GetMapping("/{id}")
    public Result<Scope> getScopeById(@PathVariable String id) {
        Scope scope = scopeService.getScopeById(id);
        if (scope == null) {
            return Result.error("作用域不存在");
        }
        return Result.success(scope);
    }

    /**
     * 根据用户ID获取作用域列表
     * @param userId 用户ID
     * @return 作用域列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<Scope>> getScopesByUserId(@PathVariable String userId) {
        return Result.success(scopeService.getScopesByUserId(userId));
    }

    /**
     * 创建作用域
     * @param scope 作用域
     * @return 作用域
     */
    @PostMapping
    public Result<Scope> addScope(@RequestBody Scope scope) {
        Scope addedScope = scopeService.addScope(scope);
        return Result.success("作用域添加成功", addedScope);
    }

    /**
     * 更新作用域
     * @param id 作用域ID
     * @param scope 作用域
     * @return 作用域
     */
    @PutMapping("/{id}")
    public Result<Scope> updateScope(@PathVariable String id, @RequestBody Scope scope) {
        scope.setId(id);
        Scope updatedScope = scopeService.updateScope(scope);
        return Result.success("作用域更新成功", updatedScope);
    }

    /**
     * 删除作用域
     * @param id 作用域ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteScopeById(@PathVariable String id) {
        int result = scopeService.deleteScopeById(id);
        if (result > 0) {
            return Result.success("作用域删除成功");
        } else {
            return Result.error("作用域删除失败");
        }
    }

}