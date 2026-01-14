package com.example.demo.service.task.impl;

import com.example.demo.mapper.task.ActionMapper;
import com.example.demo.mapper.task.ExtractPositionMapper;
import com.example.demo.mapper.task.RoleMapper;
import com.example.demo.pojo.task.Role;
import com.example.demo.service.task.ActionService;
import com.example.demo.service.task.RoleService;
import com.example.demo.utils.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * 任务触发规则服务实现类
 */
@Service
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final ActionMapper actionMapper;
    private final RoleMapper roleMapper;
    private final ExtractPositionMapper extractPositionMapper;
    private final ActionService actionService;

    @Autowired
    public RoleServiceImpl(ActionMapper actionMapper, RoleMapper roleMapper, ExtractPositionMapper extractPositionMapper, ActionService actionService) {
        this.actionMapper = actionMapper;
        this.roleMapper = roleMapper;
        this.extractPositionMapper = extractPositionMapper;
        this.actionService = actionService;
    }

    /**
     * 获取所有任务触发规则，按作用域分组
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
            role.setExtractPosition(extractPositionMapper.getExtractPosition(role.getId()));
            // 确保结果集中存在当前作用域ID对应的列表，如果不存在则创建新的空列表
            result.put(role.getScopeId(), result.getOrDefault(role.getScopeId(), new ArrayList<>()));
            // 将当前角色添加到对应作用域ID的列表中
            result.get(role.getScopeId()).add(role);
        }

        return result;
    }

    /**
     * 获取所有任务触发规则列表
     * @return 任务触发规则列表
     */
    @Override
    public List<Role> getAllRolesList() {
        List<Role> roles = roleMapper.getAllRoles();
        for (Role role : roles) {
            role.setAction(actionMapper.getActions(role.getId()));
            role.setExtractPosition(extractPositionMapper.getExtractPosition(role.getId()));
        }
        return roles;
    }

    /**
     * 根据ID获取任务触发规则
     * @param id 任务触发规则ID
     * @return 任务触发规则
     */
    @Override
    public Role getRoleById(String id) {
        Role role = roleMapper.selectById(id);
        if (role != null) {
            role.setAction(actionMapper.getActions(role.getId()));
            role.setExtractPosition(extractPositionMapper.getExtractPosition(role.getId()));
        }
        return role;
    }

    /**
     * 根据用户ID获取任务触发规则列表
     * @param userId 用户ID
     * @return 任务触发规则列表
     */
    @Override
    public List<Role> getRolesByUserId(String userId) {
        List<Role> roles = roleMapper.selectByUserId(userId);
        for (Role role : roles) {
            role.setAction(actionMapper.getActions(role.getId()));
            role.setExtractPosition(extractPositionMapper.getExtractPosition(role.getId()));
        }
        return roles;
    }

    /**
     * 根据作用域ID获取任务触发规则列表
     * @param scopeId 作用域ID
     * @return 任务触发规则列表
     */
    @Override
    public List<Role> getRolesByScopeId(String scopeId) {
        List<Role> roles = roleMapper.selectByScopeId(scopeId);
        for (Role role : roles) {
            role.setAction(actionMapper.getActions(role.getId()));
            role.setExtractPosition(extractPositionMapper.getExtractPosition(role.getId()));
        }
        return roles;
    }

    /**
     * 添加任务触发规则
     * @param role 任务触发规则
     * @return 任务触发规则
     */
    @Override
    public Role addRole(Role role) {
        try {
            role.setMD5(MD5Util.calculateClassMD5(role));
        } catch (NoSuchAlgorithmException e) {
            log.warn("MD5计算失败", e);
        }
        role.setId(UUID.randomUUID().toString());
        if (role.isExtract()){
            extractPositionMapper.insert(role.getId(),role.getExtractPosition());
        }
        roleMapper.insert(role);
        return role;
    }

    /**
     * 判断是否存在相同MD5的任务触发规则
     * @param md5 任务触发规则MD5
     * @return 是否存在相同MD5的任务触发规则
     */
    @Override
    public Boolean existsByMd5(String md5) {
        return roleMapper.existsByMd5(md5);
    }

    /**
     * 更新任务触发规则
     * @param role 任务触发规则
     * @return 任务触发规则
     */
    @Override
    public Role updateRole(Role role) {
        roleMapper.update(role);
        if (role.getExtractPosition() != null){
            extractPositionMapper.deleteByRoleId(role.getId());
            extractPositionMapper.insert(role.getId(),role.getExtractPosition());
        }else{
            extractPositionMapper.deleteByRoleId(role.getId());
        }
        return role;
    }

    /**
     * 删除任务触发规则，级联删除关联的Action和ExtractPosition
     * @param id 任务触发规则ID
     * @return 删除数量
     */
    @Override
    @Transactional
    public int deleteRoleById(String id) {
        // 级联删除关联的Action
        actionService.deleteActionsByRoleId(id);
        // 级联删除关联的ExtractPosition
        extractPositionMapper.deleteByRoleId(id);
        // 删除Role
        return roleMapper.deleteById(id);
    }

    /**
     * 根据作用域ID删除任务触发规则，级联删除关联的Action和ExtractPosition
     * @param scopeId 作用域ID
     * @return 删除数量
     */
    @Override
    @Transactional
    public int deleteRolesByScopeId(String scopeId) {
        // 获取该作用域下的所有Role
        List<Role> roles = roleMapper.selectByScopeId(scopeId);
        // 遍历Role，级联删除关联的Action和ExtractPosition
        for (Role role : roles) {
            actionService.deleteActionsByRoleId(role.getId());
            extractPositionMapper.deleteByRoleId(role.getId());
        }
        // 删除Role
        return roleMapper.deleteByScopeId(scopeId);
    }
}
