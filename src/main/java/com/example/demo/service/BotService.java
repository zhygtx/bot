package com.example.demo.service;

import java.util.List;
import java.util.Set;

/**
 * 机器人服务接口
 */
public interface BotService {

    /**
     * 获取所有机器人QQ
     * @return 机器人QQ列表
     */
    Set<Long> getBotQQs();

}
