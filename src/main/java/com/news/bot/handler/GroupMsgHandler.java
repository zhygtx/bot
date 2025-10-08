package com.news.bot.handler;

import com.mikuac.shiro.annotation.GroupMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.news.bot.utils.BotUtil;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;

@Component
@Shiro
public class GroupMsgHandler {

    @GroupMessageHandler
    @MessageHandlerFilter(cmd = "^设置头衔\\s(.*)?$|取消头衔", groups = {946151805})
    public void setGroupSpecialTitle(Bot bot, GroupMessageEvent event, Matcher matcher) {
        if(!"owner".equals(BotUtil.getBotRole(bot, event))){
            bot.sendGroupMsg(event.getGroupId(), "只有Bot是群主时才能使用此命令", false);
            return;
        }
        if ("取消头衔".equals(matcher.group(0))) {
            bot.setGroupSpecialTitle(event.getGroupId(), event.getUserId(), "", -1);
            bot.sendGroupMsg(event.getGroupId(), "已取消头衔", false);
        } else if (matcher.group(1) != null) {
            String title = matcher.group(1).trim();
            bot.setGroupSpecialTitle(event.getGroupId(), event.getUserId(), title, -1);
            bot.sendGroupMsg(event.getGroupId(), "已设置头衔为：" + title, false);
        } else {
            bot.sendGroupMsg(event.getGroupId(), "命令格式错误，请使用：取消头衔 或 设置头衔 新头衔", false);
        }
    }

}