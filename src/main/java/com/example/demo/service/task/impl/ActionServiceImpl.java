package com.example.demo.service.task.impl;

import com.example.demo.mapper.task.ActionMapper;
import com.example.demo.mapper.task.ReceiverMapper;
import com.example.demo.pojo.task.Action;
import com.example.demo.pojo.task.Receiver;
import com.example.demo.service.task.ActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * 动作服务实现类
 */
@Service
public class ActionServiceImpl implements ActionService {

    private final ActionMapper actionMapper;
    private final ReceiverMapper receiverMapper;

    @Autowired
    public ActionServiceImpl(ActionMapper actionMapper, ReceiverMapper receiverMapper) {
        this.actionMapper = actionMapper;
        this.receiverMapper = receiverMapper;
    }


    /**
     * 获取动作列表
     * @param roleId 规则ID
     * @return 动作列表
     */
    @Override
    public List<Action> getActions(String roleId){
        List<Action> actions = actionMapper.getActions(roleId);
        for (Action action : actions) {
            action.setReceivers(receiverMapper.getByActionId(action.getId()));
        }
        return actions;
    }

    /**
     * 根据ID获取动作
     * @param id 动作ID
     * @return 动作
     */
    @Override
    public Action getActionById(String id) {
        Action action = actionMapper.selectById(id);
        action.setReceivers(receiverMapper.getByActionId(action.getId()));
        return action;
    }

    /**
     * 根据用户ID获取动作列表
     * @param userId 用户ID
     * @return 动作列表
     */
    @Override
    public List<Action> getActionsByUserId(String userId) {
        List<Action> actions = actionMapper.selectByUserId(userId);
        for (Action action : actions) {
            action.setReceivers(receiverMapper.getByActionId(action.getId()));
        }
        return actions;
    }

    /**
     * 添加动作
     * @param action 动作
     * @return 动作
     */
    @Override
    public Action addAction(Action action) {
        action.setId(UUID.randomUUID().toString());
        actionMapper.insert(action);
        for (Receiver receiver : action.getReceivers()){
            receiver.setId(UUID.randomUUID().toString());
            receiver.setUserId(action.getUserId());
        }
        receiverMapper.insert(action.getReceivers());
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
        receiverMapper.deleteByActionId(action.getId());
        for (Receiver receiver : action.getReceivers()){
            receiver.setId(UUID.randomUUID().toString());
            receiver.setUserId(action.getUserId());
        }
        receiverMapper.insert(action.getReceivers());
        return action;
    }

    /**
     * 根据ID删除动作
     * @param id 动作ID
     */
    @Override
    public void deleteActionById(String id) {
        actionMapper.deleteById(id);
        receiverMapper.deleteByActionId(id);
    }

    /**
     * 根据规则ID删除动作
     * @param roleId 规则ID
     * @return 删除数量
     */
    @Override
    public int deleteActionsByRoleId(String roleId) {
        List<Action> actions = actionMapper.getActions(roleId);
        for (Action action : actions){
            receiverMapper.deleteByActionId(action.getId());
        }
        return actionMapper.deleteByRoleId(roleId);
    }

}
