package com.example.demo.service.task;

import com.example.demo.pojo.task.Action;

import java.util.List;

/**
 * 动作服务接口
 */
public interface ActionService {

    /**
     * 根据规则ID获取动作列表
     * @param roleId 规则ID
     * @return 动作列表
     */
    List<Action> getActions(String roleId);

    /**
     * 根据ID获取动作
     * @param id 动作ID
     * @return 动作
     */
    Action getActionById(String id);

    /**
     * 根据用户ID获取动作列表
     * @param userId 用户ID
     * @return 动作列表
     */
    List<Action> getActionsByUserId(String userId);

    /**
     * 添加动作
     * @param action 动作
     * @return 动作
     */
    Action addAction(Action action);

    /**
     * 更新动作
     * @param action 动作
     * @return 动作
     */
    Action updateAction(Action action);

    /**
     * 根据ID删除动作
     * @param id 动作ID
     * @return 删除数量
     */
    int deleteActionById(String id);

    /**
     * 根据规则ID删除动作
     * @param roleId 规则ID
     * @return 删除数量
     */
    int deleteActionsByRoleId(String roleId);

}
