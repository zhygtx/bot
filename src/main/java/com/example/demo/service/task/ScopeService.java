package com.example.demo.service.task;

import com.example.demo.pojo.task.Scope;

import java.util.List;

/**
 * 作用域服务接口
 */
public interface ScopeService {

    /**
     * 获取所有作用域
     * @return 作用域列表
     */
    List<Scope> getAllScopes();

    /**
     * 根据ID获取作用域
     * @param id 作用域ID
     * @return 作用域
     */
    Scope getScopeById(String id);

    /**
     * 根据用户ID获取作用域列表
     * @param userId 用户ID
     * @return 作用域列表
     */
    List<Scope> getScopesByUserId(String userId);

    /**
     * 添加作用域
     * @param scope 作用域
     * @return 作用域
     */
    Scope addScope(Scope scope);

    /**
     * 更新作用域
     * @param scope 作用域
     * @return 作用域
     */
    Scope updateScope(Scope scope);

    /**
     * 删除作用域
     * @param id 作用域ID
     * @return 删除数量
     */
    int deleteScopeById(String id);

}
