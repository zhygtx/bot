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
    public void sendGroupMsg(Long botQQ, Long groupId, String msg) {
        Bot bot = botContainer.robots.get(botQQ);
        bot.sendGroupMsg(groupId, msg, false);
    }

    @Override
    public void sendPrivateMsg(Long botQQ, Long userId, String msg) {
        Bot bot = botContainer.robots.get(botQQ);
        bot.sendPrivateMsg(userId, msg, false);
    }
}
