package com.example.demo.service.task;

import com.example.demo.pojo.task.Role;

import java.util.List;
import java.util.Map;

public interface RoleService {

    /**
     * 获取所有任务触发规则
     * @return 所有任务触发规则
     */
    Map<String,List<Role>> getAllRoles();

}
