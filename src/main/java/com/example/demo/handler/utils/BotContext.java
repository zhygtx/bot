package com.example.demo.handler.utils;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.common.ActionList;
import com.mikuac.shiro.dto.action.response.GroupInfoResp;
import com.mikuac.shiro.dto.action.response.GroupMemberInfoResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class BotContext {

    /**
     * 获取Bot的群权限
     * @param bot  Bot对象
     * @return 群权限
     */
    public Map<Long, String> getBotGroupRoles(Bot bot) {
        Long botId = bot.getSelfId();
        Map<Long, String> roles = new HashMap<>();
        ActionList<GroupInfoResp> groups = bot.getGroupList();
        List<GroupInfoResp> groupInfoResp = groups.getData();
        List<Long> groupIds =
                groupInfoResp.stream()
                .map(GroupInfoResp::getGroupId)
                .toList();
        for (Long groupId : groupIds){
            ActionList<GroupMemberInfoResp> memberList = bot.getGroupMemberList(groupId);
            List<GroupMemberInfoResp> memberInfoResp = memberList.getData();
            String role = "";
            for (GroupMemberInfoResp member : memberInfoResp){
                if (member.getUserId().equals(botId)){
                    role = member.getRole();
                    log.debug("bot:[{}]在群:[{}]中的权限的为{}", botId, groupId, role);
                }
            }
            if (role.isEmpty()){
                role = "member";
                log.debug("bot:[{}]在群:[{}]中的权限获取失败，设置为默认值{}", botId, groupId, role);
            }
            roles.put(groupId, role);
        }
        return roles;
    }
}