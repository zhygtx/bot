package com.example.demo.service.task;

import com.example.demo.pojo.task.Action;

import java.util.Set;

/**
 * 动作服务接口
 */
public interface ActionService {

    /**
     * 根据规则ID获取动作列表
     * @param roleId 规则ID
     * @return 动作列表
     */
    Set<Action> getActions(String roleId);

}
