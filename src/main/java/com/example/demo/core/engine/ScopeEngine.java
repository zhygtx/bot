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
                    && (scope.isAt()==groupMsg.isAt()|| !scope.isAt())
                    && scope.getQQUserRole().hasPermission(groupMsg.getUserRole())
                    && (scope.getQQUserId() == null || scope.getQQUserId().equals(groupMsg.getUserId()))
                    && (scope.getQQGroupId() == null || scope.getQQGroupId().equals(groupMsg.getGroupId()))
                    && scope.getQQBotRole().hasPermission(botCoreEvent.getBotGroupRoles(groupMsg.getBotId()).get(groupMsg.getGroupId()))
                    && (scope.getQQScopeType() == Scope.ScopeType.groupMsg || scope.getQQScopeType() == Scope.ScopeType.all)
                    && (scope.getQQScopeId() == null || scope.getQQScopeId().equals(groupMsg.getGroupId()))
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
                && (scope.getQQScopeType() == Scope.ScopeType.privateMsg || scope.getQQScopeType() == Scope.ScopeType.all)
                && (scope.getQQScopeId() == null || scope.getQQScopeId().equals(privateMsg.getUserId()))
            ){
                roles.addAll(scope.getRoles());
            }
        }

        // 过滤掉没有启用的规则
        roles.removeIf(role -> !role.isEnable());

        return roles;
    }
}
