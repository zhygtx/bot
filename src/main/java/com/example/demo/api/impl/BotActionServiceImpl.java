package com.example.demo.api.impl;

import com.example.demo.api.BotActionService;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class BotActionServiceImpl implements BotActionService {

    @Resource
    private BotContainer botContainer;

    @Override
    public void sendGroupMsg(Long botId, Long groupId, String msg) {
        Bot bot = botContainer.robots.get(botId);
        bot.sendGroupMsg(groupId, msg, false);
    }

    @Override
    public void sendPrivateMsg(Long botId, Long userId, String msg) {
        Bot bot = botContainer.robots.get(botId);
        bot.sendPrivateMsg(userId, msg, false);
    }
}
