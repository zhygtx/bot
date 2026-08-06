package com.generalbot.bot.service;

import com.generalbot.bot.entity.BotInfo;
import com.generalbot.common.api.Result;

import java.util.List;
import java.util.Map;

/**
 * 机器人服务接口
 */
public interface BotService {

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
     * @param botQQ 用户ID
     */
    void delete(Long botQQ);

    /**
     * 更新机器人
     * @param botInfo bot实体类信息
     */
    void update(BotInfo botInfo);

    /**
     * 获取机器人信息
     * @param userId 用户ID
     * @return 机器人信息
     */
    BotInfo select(String userId);

    /**
     * 获取机器人关联邮箱
     * @param botQQ botQQ号
     * @return 机器人关联的邮箱
     */
    String selectEmail(Long botQQ);

    /**
     * 获取所有机器人的路径与token
     * @return 所有机器人的路径与token
     */
    List<Map<String , String >> selectPathSuffix();
}
