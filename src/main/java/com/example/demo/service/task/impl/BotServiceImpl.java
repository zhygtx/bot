package com.example.demo.service.task.impl;

import com.example.demo.service.task.BotService;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 机器人服务实现类
 */
@Service
public class BotServiceImpl implements BotService {

    @Override
    public Set<Long> getAllBotQQs() {
        // TODO: Mapper获取机器人QQ
        return Set.of(3845884126L);
    }

}
