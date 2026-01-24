package com.example.demo.core.engine;

import com.example.demo.cache.ScopeCacheManager;
import com.example.demo.core.manager.ScopeManager;
import com.example.demo.handler.BotCoreEvent;
import com.example.demo.pojo.event.Event;
import com.example.demo.pojo.event.GroupEvent;
import com.example.demo.pojo.event.GroupMsg;
import com.example.demo.pojo.event.PrivateMsg;
import com.example.demo.pojo.task.Role;
import com.example.demo.pojo.task.Scope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
        Event event = (Event) msg;
        //先进行基础通用过滤
        List<Scope> scopes = getScopes();
        List<Scope> scopeList = scopes.stream()
                .filter(scope -> Objects.equals(scope.getBotQQ(), event.getBotId()))
                .filter(scope -> scope.getQqScopeType() == Scope.ScopeType.All ||
                        event.getEventType().toString().startsWith(scope.getQqScopeType().toString()))
                .toList();
        //获取作用域中获取到的具体规则
        return switch (event.getEventType()) {
            case GroupMsg -> getRoles((GroupMsg) msg, scopeList);
            case PrivateMsg -> getRoles((PrivateMsg) msg, scopeList);
            case GroupIncrease, GroupDecrease, GroupAddRequest -> getRoles((GroupEvent) msg, scopeList);
        };
    }

    /**
     * 获取作用域中获取到的具体规则
     * @param groupMsg 群消息对象
     * @return 作用域中获取到的具体规则
     */
    private List<Role> getRoles(GroupMsg groupMsg,List<Scope> scopeList) {
        log.info("处理群消息，群ID: {}, 用户ID: {}, 用户权限: {} ,Bot权限: {}, \n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t" +
                        "消息内容: {}",
                groupMsg.getGroupId(), groupMsg.getUserId(), groupMsg.getUserRole(),
                botCoreEvent.getBotGroupRoles(groupMsg.getBotId()).get(groupMsg.getGroupId()),
                groupMsg.getContent()
        );

        return scopeList.stream()
                .filter(scope -> scope.isAt() == groupMsg.isAt() || !scope.isAt())
                .filter(scope -> scope.getQqUserRole().hasPermission(groupMsg.getUserRole()))
                .filter(scope -> scope.getQqBotRole().hasPermission(
                        botCoreEvent.getBotGroupRoles(groupMsg.getBotId()).get(groupMsg.getGroupId())))
                .filter(scope -> scope.getQqScopeId() == null ||
                        scope.getQqScopeId().equals(groupMsg.getGroupId()))
                .flatMap(scope -> scope.getRoles().stream())
                .collect(Collectors.toList());
    }

    /**
     * 获取作用域中获取到的具体规则
     * @param privateMsg 私聊消息对象
     * @return 作用域中获取到的具体规则
     */
    private List<Role> getRoles(PrivateMsg privateMsg,List<Scope> scopeList) {
        log.debug("处理私聊消息，用户ID: {}", privateMsg.getUserId());

        return scopeList.stream()
                .filter(scope -> scope.getQqScopeId() == null ||
                        scope.getQqScopeId().equals(privateMsg.getUserId()))
                .flatMap(scope -> scope.getRoles().stream())
                .collect(Collectors.toList());
    }

    /**
     * 获取作用域中获取到的具体规则
     * @param groupEvent 群事件对象
     * @return 作用域中获取到的具体规则
     */
    private List<Role> getRoles(GroupEvent groupEvent, List<Scope> scopeList) {
        log.debug("处理群事件，群ID: {}, 用户ID: {}, 操作者ID: {}", groupEvent.getGroupId(), groupEvent.getUserId(), groupEvent.getOperatorId());
        return scopeList.stream()
                .filter(scope -> scope.getQqScopeId() == null ||
                        scope.getQqScopeId().equals(groupEvent.getGroupId()))
                .flatMap(scope -> scope.getRoles().stream())
                .collect(Collectors.toList());
    }



    /**
     * 获取并过滤作用域列表
     * @return 作用域列表
     */
    private List<Scope> getScopes() {
        List<Scope> scopes = scopeCacheManager.getCachedScopes();
        log.debug("初始作用域数量: {}", scopes.size());

        int initialSize = scopes.size();
        // 过滤掉作用域中没有启用的规则
        scopes.forEach(scope -> {
            if (scope.getRoles() != null) {
                scope.getRoles().removeIf(role -> !role.isEnable());
            }
        });
        // 过滤掉没有规则的域
        scopes.removeIf(scope -> scope.getRoles() == null || scope.getRoles().isEmpty());
        log.debug("过滤无规则作用域后数量: {} (移除了 {} 个)", scopes.size(), initialSize - scopes.size());
        return scopes;
    }
}
