package com.example.demo.service.task.impl;

import com.example.demo.mapper.task.ActionMapper;
import com.example.demo.mapper.task.RoleMapper;
import com.example.demo.pojo.task.Role;
import com.example.demo.service.task.ExtractPositionService;
import com.example.demo.service.task.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务触发规则服务实现类
 */
@Service
public class RoleServiceImpl implements RoleService {

    private final ActionMapper actionMapper;
    private final RoleMapper roleMapper;
    private final ExtractPositionService extractPositionService;

    @Autowired
    public RoleServiceImpl(ActionMapper actionMapper, RoleMapper roleMapper, ExtractPositionService extractPositionService) {
        this.actionMapper = actionMapper;
        this.roleMapper = roleMapper;
        this.extractPositionService = extractPositionService;
    }

    /**
     * 获取所有任务触发规则
     * @return 所有任务触发规则
     */
    @Override
    public Map<String, List<Role>> getAllRoles() {
        Map<String, List<Role>> result = new HashMap<>();
        List<Role> roles = roleMapper.getAllRoles();
        // 遍历角色列表，为每个角色设置对应的操作权限，并按作用域ID分组存储到结果集中
        for (Role role : roles){
            // 获取当前角色对应的所有操作权限并设置到角色对象中
            role.setAction(actionMapper.getActions(role.getId()));
            // 获取当前规则所需要的提取位置
            role.setExtractPosition(extractPositionService.getExtractPosition(role.getId()));
            // 确保结果集中存在当前作用域ID对应的列表，如果不存在则创建新的空列表
            result.put(role.getScopeId(), result.getOrDefault(role.getScopeId(), new ArrayList<>()));
            // 将当前角色添加到对应作用域ID的列表中
            result.get(role.getScopeId()).add(role);
        }

        return result;
    }
}
