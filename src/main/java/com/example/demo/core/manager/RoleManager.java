package com.example.demo.core.manager;

import com.example.demo.pojo.msg.GroupMsg;
import com.example.demo.pojo.task.Action;
import com.example.demo.pojo.task.Role;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RoleManager {

    /**
     * 根据作用域中获取到的具体规则获取动作
     * @param roles 作用域中获取到的具体规则
     * @param groupMsg 消息对象
     * @return 获取到的需要执行的动作
     */
    Map<String, List<Action>> getActions(Set<Role> roles, GroupMsg groupMsg);

}
