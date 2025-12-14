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

    @Override
    public List<Scope> getAllScopes() {
        List<Scope> scopes = scopeMapper.getAllScopes();
        Map<String, List<Role>> allRoles = roleService.getAllRoles();
        for (Scope scope : scopes){
            scope.setRoles(new ArrayList<>(allRoles.get(scope.getId())));
        }
        return scopes;
    }

}
