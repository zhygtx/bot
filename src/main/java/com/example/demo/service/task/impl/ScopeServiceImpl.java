package com.example.demo.service.task.impl;

import com.example.demo.mapper.task.*;
import com.example.demo.pojo.task.Action;
import com.example.demo.pojo.task.Receiver;
import com.example.demo.pojo.task.Role;
import com.example.demo.pojo.task.Scope;
import com.example.demo.service.task.RoleService;
import com.example.demo.service.task.ScopeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ScopeServiceImpl implements ScopeService {

    private final RoleService roleService;
    private final ScopeMapper scopeMapper;
    private final ReceiverMapper receiverMapper;
    private final ActionMapper actionMapper;
    private final RoleMapper roleMapper;
    private final ExtractPositionMapper extractPositionMapper;

    public ScopeServiceImpl(RoleService roleService, ScopeMapper scopeMapper, ReceiverMapper receiverMapper, ActionMapper actionMapper, RoleMapper roleMapper, ExtractPositionMapper extractPositionMapper) {
        this.roleService = roleService;
        this.scopeMapper = scopeMapper;
        this.receiverMapper = receiverMapper;
        this.actionMapper = actionMapper;
        this.roleMapper = roleMapper;
        this.extractPositionMapper = extractPositionMapper;
    }

    /**
     * 获取所有作用域
     * @return 作用域列表
     */
    @Override
    public List<Scope> getAllScopes() {
        List<Scope> scopes = scopeMapper.getAllScopes();
        return getScope(scopes);
    }

    /**
     * 根据ID获取作用域
     * @param id 作用域ID
     * @return 作用域
     */
    @Override
    public Scope getScopeById(String id) {
        Scope scope = scopeMapper.selectById(id);
        List<Scope> scopes = getScope(Collections.singletonList(scope));
        return scopes.get(0) != null ? scopes.get(0) : new Scope();
    }

    /**
     * 根据用户ID获取作用域列表
     * @param userId 用户ID
     * @return 作用域列表
     */
    @Override
    public List<Scope> getScopesByUserId(String userId) {
        List<Scope> scopes = scopeMapper.selectByUserId(userId);
        return getScope(scopes);
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


    /**
     * 获取作用域列表
     * @param scopes 空的作用域列表
     * @return 填满数据的作用域列表
     */
    private List<Scope> getScope(List<Scope> scopes){
        // 获取所有数据，避免多次查询数据库
        List<Receiver> receivers = receiverMapper.getAll();
        List<Action> actions = actionMapper.getAll();
        List<Role> roles = roleMapper.getAll();
        List<Map<String, Integer>> extractPositionList = extractPositionMapper.getAll();
        Map<String, Set<Integer>> extractPositionMap = new HashMap<>();
        // 填充 extractPositionMap
        for (Map<String, Integer> map : extractPositionList) {
            String roleId = String.valueOf(map.get("role_id"));
            Integer position = map.get("extract_position");

            extractPositionMap.computeIfAbsent(roleId, k -> new HashSet<>()).add(position);
        }
        // 填充Action
        for (Action action : actions){
            action.setReceivers(
                    receivers.stream().
                    filter(receiver -> receiver.getActionId().equals(action.getId()))
                    .toList()
            );
        }
        // 填充Role
        for (Role role : roles){
            role.setAction(
                    actions.stream().
                    filter(action -> action.getRoleId().equals(role.getId()))
                    .toList()
            );
            role.setExtractPosition(
                    extractPositionMap.get(role.getId())
            );
        }
        // 填充Scope
        for (Scope scope : scopes){
            scope.setRoles(
                    roles.stream().
                    filter(role -> role.getScopeId().equals(scope.getId()))
                    .toList()
            );
        }
        return scopes;
    }
}
