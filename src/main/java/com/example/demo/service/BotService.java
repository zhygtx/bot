package com.example.demo.service;

import com.example.demo.pojo.entity.BotInfo;
import com.example.demo.pojo.entity.Result;

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

    /**
     * 插入机器人
     * @param name 机器人名称
     * @param botQQ 机器QQ
     */
    Result<?> insert(String userId, String name, Long botQQ);

    /**
     * 删除机器人
     * @param userId 用户ID
     */
    void delete(String userId);

    /**
     * 更新机器人
     * @param userId 用户ID
     * @param name 机器人名称
     * @param botQQ 机器QQ
     */
    void update(String userId, String name, Long botQQ);

    /**
     * 获取机器人信息
     * @param userId 用户ID
     * @return 机器人信息
     */
    BotInfo select(String userId);
}
