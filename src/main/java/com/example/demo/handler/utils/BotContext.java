package com.example.demo.handler.utils;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.common.ActionList;
import com.mikuac.shiro.dto.action.response.GroupInfoResp;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BotContext {

    public Map<Long, String> getBotGroupRoles(Bot bot) {
        Map<Long, String> roles = new HashMap<>();
        ActionList<GroupInfoResp> groups = bot.getGroupList();
        List<GroupInfoResp> groupInfoResp = groups.getData();
        List<Long> groupIds =
                groupInfoResp.stream()
                .map(GroupInfoResp::getGroupId)
                .toList();
        for (Long groupId : groupIds){
            String role = bot.getGroupMemberInfo(groupId, bot.getSelfId(), false)
                             .getData().getRole();
            roles.put(groupId, role);
        }
        return roles;
    }
}