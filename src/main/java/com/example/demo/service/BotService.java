package com.example.demo.service;

import java.util.Set;

/**
 * 机器人服务接口
 */
public interface BotService {

    /**
     * 获取所有机器人QQ
     * @return 机器人QQ列表
     */
    Set<Long> getAllBotQQs();

    /**
     * 更新机器人在线状态
     * @param botQQ 机器人QQ
     * @param online 是否在线
     */
    void updateOnline(Long botQQ, boolean online);

}
