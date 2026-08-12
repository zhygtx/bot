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
     * 更新机器人在线状态，并记录本次上线时间戳。
     * 上线时由 BotCoreEvent 传入当前时间；离线时传入 null 清空在线时长起点。
     * @param botQQ 机器人QQ
     * @param online 是否在线
     * @param onlineSince 本次上线时间戳（毫秒），离线时为 null
     */
    void updateOnline(Long botQQ, boolean online, Long onlineSince);

    /**
     * 系统关闭时将所有 Bot 标记为离线，并清空在线时长起点。
     */
    void markAllOffline();

    /**
     * 插入机器人
     * @param name 机器人名称
     * @param botQQ 机器QQ
     * @param token 自定义连接 token，为空时由后端随机生成
     */
    Result<?> insert(String userId, String name, Long botQQ, String token);

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
