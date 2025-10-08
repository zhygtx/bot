package com.news.bot.handler;

import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.PrivateMessageHandler;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.news.bot.pojo.GroupPermission;
import com.news.bot.service.GroupPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.regex.Matcher;

@Slf4j
@Component
@Shiro
public class PrivateMsgHandler {

    @Autowired
    private GroupPermissionService groupPermissionService;

    @PrivateMessageHandler
    @MessageHandlerFilter(cmd = "^添加权限\\s+(\\d+)\\s+(\\S+)$",senders = {1874743565})
    public void insertGroupPermission(Bot bot, PrivateMessageEvent event, Matcher matcher){
        GroupPermission groupPermission = new GroupPermission();
        String groupId = matcher.group(1);  // 获取群号
        String permission = matcher.group(2);  // 获取权限
        groupPermission.setGroupId(Long.parseLong(groupId));
        groupPermission.setPermission(permission);
        groupPermission.setCreateTime(LocalDateTime.now());
        try {
            groupPermissionService.insert(groupPermission);
            bot.sendPrivateMsg(event.getUserId(),"为"+groupId+"添加权限"+permission+"成功", false);
        }catch (Exception e){
            log.error("添加权限失败：{}",e.getMessage());
        }
    }

    @PrivateMessageHandler
    @MessageHandlerFilter(cmd = "^删除权限(?:\\s+(\\S+))(?:\\s+(\\S+))?$",senders = {1874743565})
    public void deleteGroupPermission(Bot bot, PrivateMessageEvent event, Matcher matcher){
        GroupPermission groupPermission = new GroupPermission();
        String arg1 = matcher.group(1);
        String arg2 = matcher.group(2);

        if (arg1 == null){
            bot.sendPrivateMsg(event.getUserId(),"请输入要删除的群权限", false);
            return;
        }

        if (arg2 == null){
            if(arg1.matches("\\d+")){
                groupPermission.setGroupId(Long.parseLong(arg1));
            }else {
                groupPermission.setPermission(arg1);
            }
        }else{
            groupPermission.setGroupId(Long.parseLong(arg1));
            groupPermission.setPermission(arg2);
        }

        try {
            groupPermissionService.delete(groupPermission);
            bot.sendPrivateMsg(event.getUserId(),"删除"+arg1+arg2+"权限成功", false);
        }catch (Exception e){
            log.error("删除权限失败：{}",e.getMessage());
        }
    }


}
