package com.example.demo.service.task.impl;

import com.example.demo.mapper.task.ScopeMapper;
import com.example.demo.pojo.task.Role;
import com.example.demo.pojo.task.Scope;
import com.example.demo.service.task.RoleService;
import com.example.demo.service.task.ScopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ScopeServiceImpl implements ScopeService {

    private final RoleService roleService;
    private final ScopeMapper scopeMapper;

    @Autowired
    public ScopeServiceImpl(RoleService roleService, ScopeMapper scopeMapper) {
        this.roleService = roleService;
        this.scopeMapper = scopeMapper;
    }

    /**
     * 获取所有作用域
     * @return 作用域列表
     */
    @Override
    public List<Scope> getAllScopes() {
        List<Scope> scopes = scopeMapper.getAllScopes();
        Map<String, List<Role>> allRoles = roleService.getAllRoles();
        for (Scope scope : scopes) {
            List<Role> roles = allRoles.get(scope.getId());
            scope.setRoles(roles != null ? new ArrayList<>(roles) : new ArrayList<>());
        }
        return scopes;
    }

    /**
     * 根据ID获取作用域
     * @param id 作用域ID
     * @return 作用域
     */
    @Override
    public Scope getScopeById(String id) {
        Scope scope = scopeMapper.selectById(id);
        if (scope != null) {
            Map<String, List<Role>> allRoles = roleService.getAllRoles();
            List<Role> roles = allRoles.get(scope.getId());
            scope.setRoles(roles != null ? new ArrayList<>(roles) : new ArrayList<>());
        }
        return scope;
    }

    /**
     * 根据用户ID获取作用域列表
     * @param userId 用户ID
     * @return 作用域列表
     */
    @Override
    public List<Scope> getScopesByUserId(String userId) {
        List<Scope> scopes = scopeMapper.selectByUserId(userId);
        Map<String, List<Role>> allRoles = roleService.getAllRoles();
        for (Scope scope : scopes) {
            List<Role> roles = allRoles.get(scope.getId());
            scope.setRoles(roles != null ? new ArrayList<>(roles) : new ArrayList<>());
        }
        return scopes;
    }

    /**
     * 添加作用域
     * @param scope 作用域
     * @return 作用域
     */
    @Override
    public Scope addScope(Scope scope) {
        scope.setId(UUID.randomUUID().toString());
        scopeMapper.insert(scope);
        return scope;
    }

    /**
     * 更新作用域
     * @param scope 作用域
     * @return 作用域
     */
    @Override
    public Scope updateScope(Scope scope) {
        scopeMapper.update(scope);
        return scope;
    }

    /**
     * 删除作用域，级联删除关联的Role、Action等
     * @param id 作用域ID
     * @return 删除数量
     */
    @Override
    @Transactional
    public int deleteScopeById(String id) {
        // 级联删除关联的Role（Role会级联删除关联的Action和ExtractPosition）
        roleService.deleteRolesByScopeId(id);
        // 删除Scope
        return scopeMapper.deleteById(id);
    }

}
