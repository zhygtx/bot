package com.example.demo.service.task.impl;

import com.example.demo.pojo.task.Role;
import com.example.demo.service.task.RoleService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RoleServiceImpl implements RoleService {

    @Override
    public Map<String, List<Role>> getAllRoles() {
        // TODO: Mapper获取所有任务触发规则
        return new HashMap<>();
    }
}
