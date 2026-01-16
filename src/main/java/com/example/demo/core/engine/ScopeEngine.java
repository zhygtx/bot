package com.example.demo.core.engine;

import com.example.demo.cache.ScopeCacheManager;
import com.example.demo.core.manager.ScopeManager;
import com.example.demo.handler.BotCoreEvent;
import com.example.demo.pojo.msg.GroupMsg;
import com.example.demo.pojo.msg.PrivateMsg;
import com.example.demo.pojo.task.Role;
import com.example.demo.pojo.task.Scope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j

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
        log.debug("开始获取规则，消息类型: {}", msg.getClass().getSimpleName());
        List<Role> result;
        if (msg.getClass() == GroupMsg.class){
            result = getRoles((GroupMsg) msg);
        } else if (msg.getClass() == PrivateMsg.class) {
            result = getRoles((PrivateMsg) msg);
        } else {
            log.debug("不支持的消息类型: {}", msg.getClass().getSimpleName());
            return null;
        }
        log.debug("获取规则完成，共获取到 {} 个规则", result.size());
        return result;
    }

    /**
     * 获取作用域中获取到的具体规则
     * @param groupMsg 群消息对象
     * @return 作用域中获取到的具体规则
     */
    private List<Role> getRoles(GroupMsg groupMsg){
        log.debug("处理群消息，群ID: {}, 用户ID: {}", groupMsg.getGroupId(), groupMsg.getUserId());
        List<Scope> scopes = scopeCacheManager.getCachedScopes();
        log.debug("初始作用域数量: {}", scopes.size());

        // 过滤掉没有规则的域
        int initialSize = scopes.size();
        scopes.removeIf(scope -> scope.getRoles() == null || scope.getRoles().isEmpty());
        log.debug("过滤无规则作用域后数量: {} (移除了 {} 个)", scopes.size(), initialSize - scopes.size());

        List<Role> roles = new ArrayList<>();
        for (Scope scope : scopes){
            log.debug("检查作用域: {}, 作用域类型: {}", scope.getId(), scope.getQqScopeType());
            if (Objects.equals(scope.getBotQQ(), groupMsg.getBotId())
                    && (scope.isAt()==groupMsg.isAt()|| !scope.isAt())
                    && scope.getQqUserRole().hasPermission(groupMsg.getUserRole())
                    && scope.getQqBotRole().hasPermission(botCoreEvent.getBotGroupRoles(groupMsg.getBotId()).get(groupMsg.getGroupId()))
                    && (scope.getQqScopeType() == Scope.ScopeType.groupMsg || scope.getQqScopeType() == Scope.ScopeType.all)
                    && (scope.getQqScopeId() == null || scope.getQqScopeId().equals(groupMsg.getGroupId()))
                    ){
                log.debug("作用域 {} 匹配成功，添加 {} 个规则", scope.getId(), scope.getRoles().size());
                roles.addAll(scope.getRoles());
            }
        }

        // 过滤掉没有启用的规则
        initialSize = roles.size();
        roles.removeIf(role -> !role.isEnable());
        log.debug("过滤禁用规则后数量: {} (移除了 {} 个)", roles.size(), initialSize - roles.size());

        return roles;
    }

    /**
     * 获取作用域中获取到的具体规则
     * @param privateMsg 私聊消息对象
     * @return 作用域中获取到的具体规则
     */
    private List<Role> getRoles(PrivateMsg privateMsg) {
        log.debug("处理私聊消息，用户ID: {}", privateMsg.getUserId());
        List<Scope> scopes = scopeCacheManager.getCachedScopes();
        log.debug("初始作用域数量: {}", scopes.size());

        // 过滤掉没有规则的域
        int initialSize = scopes.size();
        scopes.removeIf(scope -> scope.getRoles() == null || scope.getRoles().isEmpty());
        log.debug("过滤无规则作用域后数量: {} (移除了 {} 个)", scopes.size(), initialSize - scopes.size());

        List<Role> roles = new ArrayList<>();
        for (Scope scope : scopes){
            log.debug("检查作用域: {}, 作用域类型: {}", scope.getId(), scope.getQqScopeType());
            if (Objects.equals(scope.getBotQQ(), privateMsg.getBotId())
                && (scope.getQqScopeType() == Scope.ScopeType.privateMsg || scope.getQqScopeType() == Scope.ScopeType.all)
                && (scope.getQqScopeId() == null || scope.getQqScopeId().equals(privateMsg.getUserId()))
            ){
                log.debug("作用域 {} 匹配成功，添加 {} 个规则", scope.getId(), scope.getRoles().size());
                roles.addAll(scope.getRoles());
            }
        }

        // 过滤掉没有启用的规则
        initialSize = roles.size();
        roles.removeIf(role -> !role.isEnable());
        log.debug("过滤禁用规则后数量: {} (移除了 {} 个)", roles.size(), initialSize - roles.size());

        return roles;
    }
}
