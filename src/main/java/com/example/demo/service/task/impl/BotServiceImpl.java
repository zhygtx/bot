package com.example.demo.service.task.impl;

import com.example.demo.mapper.BotMapper;
import com.example.demo.service.task.BotService;
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

}
