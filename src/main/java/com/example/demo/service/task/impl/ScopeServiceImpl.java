package com.example.demo.service.task.impl;

import com.example.demo.pojo.task.Role;
import com.example.demo.pojo.task.Scope;
import com.example.demo.service.task.RoleService;
import com.example.demo.service.task.ScopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ScopeServiceImpl implements ScopeService {

    private final RoleService roleService;

    @Autowired
    public ScopeServiceImpl(RoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    public Set<Scope> getAllScopes() {
        Set<Scope> scopes = new HashSet<>();//TODO: Mapper获取所有任务触发域，先暂时创建一个空的Set
        Map<String, List<Role>> allRoles = roleService.getAllRoles();
        for (Scope scope : scopes){
            scope.setRoles(new HashSet<>(allRoles.get(scope.getId())));
        }
        return scopes;
    }

}
