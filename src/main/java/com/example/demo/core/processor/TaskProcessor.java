package com.example.demo.core.processor;

import com.example.demo.core.manager.ActionManager;
import com.example.demo.core.manager.ReceiverManager;
import com.example.demo.core.manager.RoleManager;
import com.example.demo.core.manager.ScopeManager;
import com.example.demo.pojo.task.Action;
import com.example.demo.pojo.task.Receiver;
import com.example.demo.pojo.task.Role;
import com.mikuac.shiro.core.Bot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 任务处理类,负责调度各模块的处理逻辑
 */
@Slf4j
@Component
public class TaskProcessor {

     private final ScopeManager scopeManager;
     private final RoleManager roleManager;
     private final ActionManager actionManager;
     private final ReceiverManager receiverManager;

    public TaskProcessor(ScopeManager scopeManager, RoleManager roleManager, ActionManager actionManager, ReceiverManager receiverManager) {
        this.scopeManager = scopeManager;
        this.roleManager = roleManager;
        this.actionManager = actionManager;
        this.receiverManager = receiverManager;
    }


    public void taskProcess(Bot bot, Object msg) {

        // 获取作用域中获取到的具体规则
        List<Role> roles = scopeManager.getRoles(msg);
        log.debug("获取作用域中获取到的具体规则: {}",roles);
        if (roles == null || roles.isEmpty()) return;

        //获取需要执行的动作
        List<Action> actions = roleManager.getActions(roles,msg);
        log.debug("获取需要执行的动作: {}",actions);
        if (actions == null || actions.isEmpty()) return;

        //构建发送内容
        Map<Receiver.ReceiverType, Map<Long,String>>  receivers= actionManager.executeActions(actions,msg);
        log.debug("构建发送内容: {}",receivers);
        if (receivers == null || receivers.isEmpty()) return;

        //发送消息
        receiverManager.sendMessage(bot,receivers);
    }
}