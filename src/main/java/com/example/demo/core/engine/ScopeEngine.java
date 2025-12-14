package com.example.demo.core.engine;

import com.example.demo.cache.ScopeCacheManager;
import com.example.demo.core.manager.ScopeManager;
import com.example.demo.handler.BotCoreEvent;
import com.example.demo.pojo.msg.GroupMsg;
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
     * 获取作用域中获取到的具体规则
     * @param groupMsg 群消息对象
     * @return 作用域中获取到的具体规则
     */
    @Override
    public List<Role> getRoles(GroupMsg groupMsg){
        List<Scope> scopes = scopeCacheManager.getCachedScopes();

        // 过滤掉没有规则的域
        scopes.removeIf(scope -> scope.getRoles() == null|| scope.getRoles().isEmpty());

        List<Role> roles = new ArrayList<>();
        for (Scope scope : scopes){
            if (Objects.equals(scope.getBotQQ(), groupMsg.getBotId())
                && (scope.isAt()==groupMsg.isAt()|| !scope.isAt())
                && scope.getUserRole().hasPermission(groupMsg.getUserRole())
                && scope.getBotRole().hasPermission(botCoreEvent.getBotGroupRoles(groupMsg.getBotId()).get(groupMsg.getGroupId()))
                ){
                roles.addAll(scope.getRoles());
            }
        }

        // 过滤掉没有启用的规则
        roles.removeIf(role -> !role.isEnable());

        return roles;
    }

}
