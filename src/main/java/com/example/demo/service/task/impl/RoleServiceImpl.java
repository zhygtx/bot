package com.example.demo.service.task.impl;

import com.example.demo.pojo.task.Role;
import com.example.demo.service.task.ActionService;
import com.example.demo.service.task.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务触发规则服务实现类
 */
@Service
public class RoleServiceImpl implements RoleService {

    private final ActionService actionService;

    @Autowired
    public RoleServiceImpl(ActionService actionService) {
        this.actionService = actionService;
    }

    /**
     * 获取所有任务触发规则
     * @return 所有任务触发规则
     */
    @Override
    public Map<String, List<Role>> getAllRoles() {
        // TODO: Mapper获取所有任务触发规则,并关联任务动作
        return new HashMap<>();
    }
}
