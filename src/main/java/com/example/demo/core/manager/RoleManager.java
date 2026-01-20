package com.example.demo.core.manager;

import com.example.demo.pojo.task.Action;
import com.example.demo.pojo.task.Role;

import java.util.List;

public interface RoleManager {

    /**
     * 根据作用域中获取到的具体规则获取动作
     * @param roles 作用域中获取到的具体规则
     * @param msgObj 消息对象
     * @return 获取到的需要执行的动作
     */
    List<Action> getActions(List<Role> roles, Object msgObj);

}
