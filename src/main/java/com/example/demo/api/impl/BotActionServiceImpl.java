package com.example.demo.api.impl;

import com.example.demo.api.BotActionService;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class BotActionServiceImpl implements BotActionService {

    @Resource
    private BotContainer botContainer;

    @Override
    public void sendGroupMsg(Long botQQ, Long groupQQ, String msg) {
        Bot bot = botContainer.robots.get(botQQ);
        bot.sendGroupMsg(groupQQ, msg, false);
    }

    @Override
    public void sendPrivateMsg(Long botQQ, Long userQQ, String msg) {
        Bot bot = botContainer.robots.get(botQQ);
        bot.sendPrivateMsg(userQQ, msg, false);
    }

    @Override
    public void sendGroupMsgBatch(Long botQQ, Map<Long, String> msg) {
        Bot bot = botContainer.robots.get(botQQ);
        for (Map.Entry<Long, String> entry : msg.entrySet()){
            Long groupId = entry.getKey();
            String message = entry.getValue();
            bot.sendGroupMsg(groupId, message, false);
        }
    }

    @Override
    public void sendPrivateMsgBatch(Long botQQ, Map<Long, String> msg) {
        Bot bot = botContainer.robots.get(botQQ);
        for (Map.Entry<Long, String> entry : msg.entrySet()){
            Long userId = entry.getKey();
            String message = entry.getValue();
            bot.sendPrivateMsg(userId, message, false);
        }
    }

    @Override
    public void setGroupSpecialTitle(Long botQQ, Long groupQQ, Long userQQ, String specialTitle) {
        Bot bot = botContainer.robots.get(botQQ);
        bot.setGroupSpecialTitle(groupQQ, userQQ, specialTitle,-1);
    }
}
