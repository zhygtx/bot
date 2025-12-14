package com.example.demo.core.processor;

import com.example.demo.core.manager.ActionManager;
import com.example.demo.core.manager.RoleManager;
import com.example.demo.core.manager.ScopeManager;
import com.example.demo.pojo.msg.GroupMsg;
import com.example.demo.pojo.task.Action;
import com.example.demo.pojo.task.Role;
import com.mikuac.shiro.common.utils.MsgUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

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


    public String taskProcess(GroupMsg groupMsg) {

        // 获取作用域中获取到的具体规则
        Set<Role> roles = scopeManager.getRoles(groupMsg);

        //获取需要执行的动作
        Map<String, List<Action>> actions = roleManager.getActions(roles, groupMsg);

        return MsgUtils.builder()
                .text("群号"+groupMsg.getGroupId()+"\n")
                .text("用户"+groupMsg.getUserId()+"\n")
                .text("用户权限"+groupMsg.getUserRole()+"\n")
                .text("BotID"+groupMsg.getBotId()+"\n")
                .text("消息类型"+groupMsg.getType()+"\n")
                .text("消息内容"+groupMsg.getContent()+"\n")
                .text("是否@Bot"+groupMsg.isAt()+"\n")
                .build();
    }
}