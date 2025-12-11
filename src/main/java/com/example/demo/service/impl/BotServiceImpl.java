package com.example.demo.service.impl;

import com.example.demo.service.BotService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * 机器人服务实现类
 */
@Service
public class BotServiceImpl implements BotService {

    @Override
    public Set<Long> getBotQQs() {
        // TODO: Mapper获取机器人QQ
        return Set.of(1874743565L, 3845884126L);
    }

}
