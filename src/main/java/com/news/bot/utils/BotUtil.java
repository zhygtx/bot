package com.news.bot.utils;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;

public class BotUtil {

    public static String getUserRole(Bot bot, GroupMessageEvent event){
        return bot.getGroupMemberInfo(event.getGroupId(), event.getUserId(), true).getData().getRole();
    }
    public static String getBotRole(Bot bot, GroupMessageEvent event){
        return bot.getGroupMemberInfo(event.getGroupId(), bot.getSelfId(), true).getData().getRole();
    }
}