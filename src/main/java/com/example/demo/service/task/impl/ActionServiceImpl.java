package com.example.demo.service.task.impl;

import com.example.demo.pojo.task.Action;
import com.example.demo.service.task.ActionService;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 动作服务实现类
 */
@Service
public class ActionServiceImpl implements ActionService {

    @Override
    public Set<Action> getActions(String roleId){
        // TODO: 从数据库获取动作
        return Set.of();
    }

}
