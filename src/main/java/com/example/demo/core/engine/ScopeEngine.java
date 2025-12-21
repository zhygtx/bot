package com.example.demo.core.engine;

import com.example.demo.cache.ScopeCacheManager;
import com.example.demo.core.manager.ScopeManager;
import com.example.demo.handler.BotCoreEvent;
import com.example.demo.pojo.msg.GroupMsg;
import com.example.demo.pojo.msg.PrivateMsg;
import com.example.demo.pojo.task.Role;
import com.example.demo.pojo.task.Scope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class ScopeEngine implements ScopeManager {

    private final BotCoreEvent botCoreEvent;
    private final ScopeCacheManager scopeCacheManager;

    @Autowired
    public ScopeEngine(ScopeCacheManager scopeCacheManager, BotCoreEvent botCoreEvent) {
        this.scopeCacheManager = scopeCacheManager;
        this.botCoreEvent = botCoreEvent;
    }

    /**
     * 根据消息对象获取作用域中获取到的具体规则
     * @param msg 消息对象
     * @return 作用域中获取到的具体规则
     */
    @Override
    public List<Role> getRoles(Object msg){
        if (msg.getClass() == GroupMsg.class){
            return getRoles((GroupMsg) msg);
        } else if (msg.getClass() == PrivateMsg.class) {
            return getRoles((PrivateMsg) msg);
        }
        return null;
    }

    /**
     * 获取作用域中获取到的具体规则
     * @param groupMsg 群消息对象
     * @return 作用域中获取到的具体规则
     */
    private List<Role> getRoles(GroupMsg groupMsg){
        List<Scope> scopes = scopeCacheManager.getCachedScopes();

        // 过滤掉没有规则的域
        scopes.removeIf(scope -> scope.getRoles() == null|| scope.getRoles().isEmpty());

        List<Role> roles = new ArrayList<>();
        for (Scope scope : scopes){
            if (Objects.equals(scope.getBotQQ(), groupMsg.getBotId())
                && (scope.getScopeType() == Scope.ScopeType.groupMsg || scope.getScopeType() == Scope.ScopeType.all)
                && (scope.isAt()==groupMsg.isAt()|| !scope.isAt())
                && scope.getUserRole().hasPermission(groupMsg.getUserRole())
                && scope.getBotRole().hasPermission(botCoreEvent.getBotGroupRoles(groupMsg.getBotId()).get(groupMsg.getGroupId()))
                && (scope.getScopeId() == null || scope.getScopeId().equals(groupMsg.getGroupId()))
                ){
                roles.addAll(scope.getRoles());
            }
        }

        // 过滤掉没有启用的规则
        roles.removeIf(role -> !role.isEnable());

        return roles;
    }

    /**
     * 获取作用域中获取到的具体规则
     * @param privateMsg 私聊消息对象
     * @return 作用域中获取到的具体规则
     */
    private List<Role> getRoles(PrivateMsg privateMsg) {
        List<Scope> scopes = scopeCacheManager.getCachedScopes();

        // 过滤掉没有规则的域
        scopes.removeIf(scope -> scope.getRoles() == null|| scope.getRoles().isEmpty());

        List<Role> roles = new ArrayList<>();
        for (Scope scope : scopes){
            if (Objects.equals(scope.getBotQQ(), privateMsg.getBotId())
                && (scope.getScopeType() == Scope.ScopeType.privateMsg || scope.getScopeType() == Scope.ScopeType.all)
                && (scope.getScopeId() == null || scope.getScopeId().equals(privateMsg.getUserId()))
            ){
                roles.addAll(scope.getRoles());
            }
        }

        // 过滤掉没有启用的规则
        roles.removeIf(role -> !role.isEnable());

        return roles;
    }
}
