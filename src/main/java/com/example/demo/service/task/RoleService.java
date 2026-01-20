package com.example.demo.service.task;

import com.example.demo.pojo.task.Role;

import java.util.List;

public interface RoleService {

    /**
     * 根据ID获取任务触发规则
     * @param id 任务触发规则ID
     * @return 任务触发规则
     */
    Role getRoleById(String id);

    /**
     * 根据用户ID获取任务触发规则列表
     * @param userId 用户ID
     * @return 任务触发规则列表
     */
    List<Role> getRolesByUserId(String userId);

    /**
     * 根据作用域ID获取任务触发规则列表
     * @param scopeId 作用域ID
     * @return 任务触发规则列表
     */
    List<Role> getRolesByScopeId(String scopeId);

    /**
     * 添加任务触发规则
     * @param role 任务触发规则
     * @return 任务触发规则
     */
    Role addRole(Role role);

    /**
     * 判断是否存在相同MD5的任务触发规则
     * @param md5 MD5值
     * @return 是否存在相同MD5的任务触发规则
     */
    Boolean existsByMd5(String md5);

    /**
     * 更新任务触发规则
     * @param role 任务触发规则
     * @return 任务触发规则
     */
    Role updateRole(Role role);

    /**
     * 删除任务触发规则
     * @param id 任务触发规则ID
     * @return 删除数量
     */
    int deleteRoleById(String id);

    /**
     * 根据作用域ID删除任务触发规则
     * @param scopeId 作用域ID
     * @return 删除数量
     */
    int deleteRolesByScopeId(String scopeId);

}
