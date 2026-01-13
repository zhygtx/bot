package com.example.demo.service.task.impl;

import com.example.demo.mapper.task.ActionMapper;
import com.example.demo.pojo.task.Action;
import com.example.demo.service.task.ActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 动作服务实现类
 */
@Service
public class ActionServiceImpl implements ActionService {

    private final ActionMapper actionMapper;

    @Autowired
    public ActionServiceImpl(ActionMapper actionMapper) {
        this.actionMapper = actionMapper;
    }


    /**
     * 获取动作列表
     * @param roleId 规则ID
     * @return 动作列表
     */
    @Override
    public List<Action> getActions(String roleId){
        return actionMapper.getActions(roleId);
    }

    /**
     * 根据ID获取动作
     * @param id 动作ID
     * @return 动作
     */
    @Override
    public Action getActionById(String id) {
        return actionMapper.selectById(id);
    }

    /**
     * 根据用户ID获取动作列表
     * @param userId 用户ID
     * @return 动作列表
     */
    @Override
    public List<Action> getActionsByUserId(String userId) {
        return actionMapper.selectByUserId(userId);
    }

    /**
     * 添加动作
     * @param action 动作
     * @return 动作
     */
    @Override
    public Action addAction(Action action) {
        actionMapper.insert(action);
        return action;
    }

    /**
     * 更新动作
     * @param action 动作
     * @return 动作
     */
    @Override
    public Action updateAction(Action action) {
        actionMapper.update(action);
        return action;
    }

    /**
     * 根据ID删除动作
     * @param id 动作ID
     * @return 删除数量
     */
    @Override
    public int deleteActionById(String id) {
        return actionMapper.deleteById(id);
    }

    /**
     * 根据规则ID删除动作
     * @param roleId 规则ID
     * @return 删除数量
     */
    @Override
    public int deleteActionsByRoleId(String roleId) {
        return actionMapper.deleteByRoleId(roleId);
    }

}
