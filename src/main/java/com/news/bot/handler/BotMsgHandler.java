package com.news.bot.handler;

import com.mikuac.shiro.annotation.GroupMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.common.ActionData;
import com.mikuac.shiro.dto.action.response.BooleanResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.news.bot.utils.BotUtil;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;

@Component
@Shiro
public class BotMsgHandler {

    @GroupMessageHandler
    @MessageHandlerFilter(cmd = "^测试\\s(.*)?$",groups = {1053302473})
    public void test(Bot bot, GroupMessageEvent event, Matcher matcher) {
        String msg = matcher.group(1);
        bot.sendGroupMsg(event.getGroupId(), msg, false);
    }


    @SuppressWarnings("StringBufferReplaceableByString")
    @GroupMessageHandler
    @MessageHandlerFilter(cmd = "自检",groups = {1053302473})
    public void checkSelf(Bot bot, GroupMessageEvent event, Matcher matcher) {
        ActionData<BooleanResp> resp = bot.canSendImage();
        String botInfo = BotUtil.getBotRole(bot, event);

        StringBuilder msg = new StringBuilder();
        msg.append("Bot身份：").append(botInfo).append("\n");
        msg.append("Bot是否可以发送图片：").append(resp.getData().getYes());

        bot.sendGroupMsg(event.getGroupId(), msg.toString(), false);
    }

}