package com.example.demo.service.impl;

import com.example.demo.mapper.BotMapper;
import com.example.demo.service.BotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 机器人服务实现类
 */
@Service
public class BotServiceImpl implements BotService {

    private final BotMapper botMapper;

    @Autowired
    public BotServiceImpl(BotMapper botMapper) {
        this.botMapper = botMapper;
    }

    /**
     * 获取所有机器人QQ
     * @return 机器人QQ列表
     */
    @Override
    public Set<Long> getAllBotQQs() {
        return botMapper.getAllBotQQs();
    }

    /**
     * 更新机器人是否在线
     * @param botQQ 机器人QQ
     * @param online 是否在线
     */
    @Override
    public void updateOnline(Long botQQ, boolean online){
        botMapper.updateOnline(botQQ, online);
    }

}
