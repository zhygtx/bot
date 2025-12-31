package com.example.demo.service.task.impl;

import com.example.demo.mapper.task.ScopeMapper;
import com.example.demo.pojo.task.Role;
import com.example.demo.pojo.task.Scope;
import com.example.demo.service.task.RoleService;
import com.example.demo.service.task.ScopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

}
