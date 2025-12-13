package com.example.demo.core.engine;

import com.example.demo.cache.ScopeCacheManager;
import com.example.demo.core.manager.ScopeManager;
import com.example.demo.handler.BotCoreEvent;
import com.example.demo.pojo.msg.GroupMsg;
import com.example.demo.pojo.task.Role;
import com.example.demo.pojo.task.Scope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Component
public class ScopeEngine implements ScopeManager {

    private final BotCoreEvent botCoreEvent;
    private final ScopeCacheManager scopeCacheManager;

    @Autowired
    public ScopeEngine(ScopeCacheManager scopeCacheManager, BotCoreEvent botCoreEvent) {
        this.scopeCacheManager = scopeCacheManager;
        this.botCoreEvent = botCoreEvent;
    }

    @Override
    public Set<Role> getRoles(GroupMsg groupMsg){
        Set<Scope> scopes = scopeCacheManager.getCachedScopes();
        Set<Role> roles = new HashSet<>();
        for (Scope scope : scopes){
            if (Objects.equals(scope.getBotQQ(), groupMsg.getBotId())
                && (scope.isAt()==groupMsg.isAt()|| !scope.isAt())
                && scope.getUserRole().hasPermission(groupMsg.getUserRole())
                && scope.getBotRole().hasPermission(botCoreEvent.getBotGroupRoles(groupMsg.getBotId()).get(groupMsg.getGroupId()))
                ){
                roles.addAll(scope.getRoles());
            }
        }
        roles.removeIf(role -> !role.isEnable());
        return roles;
    }

}
