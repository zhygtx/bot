package com.example.demo.core.processor;

import com.example.demo.core.manager.ActionManager;
import com.example.demo.core.manager.RoleManager;
import com.example.demo.core.manager.ScopeManager;
import com.example.demo.pojo.task.Action;
import com.example.demo.pojo.task.Role;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 任务处理类,负责调度各模块的处理逻辑
 */
@Component
public class TaskProcessor {

     private final ScopeManager scopeManager;

     private final RoleManager roleManager;

     private final ActionManager actionManager;

    public TaskProcessor(ScopeManager scopeManager, RoleManager roleManager, ActionManager actionManager) {
        this.scopeManager = scopeManager;
        this.roleManager = roleManager;
        this.actionManager = actionManager;
    }


    public List<String> taskProcess(Object msg) {

        // 获取作用域中获取到的具体规则
        List<Role> roles = scopeManager.getRoles(msg);

        //获取需要执行的动作
        Map<String, List<Action>> actions = roleManager.getActions(roles,msg);

        //执行相关动作与构建发送内容
        return actionManager.executeActions(actions,msg);
    }
}