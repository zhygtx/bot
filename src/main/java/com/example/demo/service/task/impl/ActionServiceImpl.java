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

}
