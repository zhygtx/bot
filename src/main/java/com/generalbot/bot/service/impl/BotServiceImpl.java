package com.generalbot.bot.service.impl;

import com.generalbot.bot.mapper.BotMapper;
import com.generalbot.bot.entity.BotInfo;
import com.generalbot.common.api.Result;
import com.generalbot.bot.service.BotService;
import com.github.zhygtx.napcat.auth.BotRegistrar;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 机器人服务实现类
 */
@Service
public class BotServiceImpl implements BotService {

    private final BotRegistrar botRegistrar;
    private final BotMapper botMapper;

    public BotServiceImpl(BotMapper botMapper, BotRegistrar botRegistrar) {
        this.botMapper = botMapper;
        this.botRegistrar = botRegistrar;
    }

    /**
     * 更新机器人是否在线，并同步本次上线时间戳。
     * onlineSince 由上下线事件提供：上线写 System.currentTimeMillis()，
     * 下线写 null；这样统计首页能直接算本次在线时长。
     * @param botQQ 机器人QQ
     * @param online 是否在线
     * @param onlineSince 本次上线时间戳（毫秒），离线时为 null
     */
    @Override
    @Transactional
    public void updateOnline(Long botQQ, boolean online, Long onlineSince){
        botMapper.updateOnline(botQQ, online, onlineSince);
    }

    @Override
    @Transactional
    public void markAllOffline() {
        botMapper.markAllOffline();
    }

    /**
     * 插入机器人
     * @param name 机器人名称
     * @param botQQ 机器人QQ
     * @param token 自定义连接 token，为空时由后端随机生成
     */
    @Override
    @Transactional
    public Result<?> insert(String userId, String name, Long botQQ, String token) {
        if (botMapper.existsByBotQQ(botQQ)) {
            return Result.error(400,"该bot已被注册");
        }
        BotInfo bot = new BotInfo();
        bot.setId(UUID.randomUUID().toString());
        bot.setBotQQ(botQQ);
        bot.setName(name);
        bot.setUserId(userId);
        bot.setPathSuffix(botQQ.toString());
        bot.setToken(token == null || token.isBlank() ? UUID.randomUUID().toString() : token.trim());
        botMapper.insert(bot);
        botRegistrar.register(botQQ.toString(), bot.getToken());
        return Result.success("添加成功",null);
    }

    /**
     * 删除机器人
      * @param botQQ 用户ID
     */
    @Override
    @Transactional
    public void delete(Long botQQ){
        botMapper.delete(botQQ);
        botRegistrar.unregister(botQQ.toString());
    }

    /**
     * 更新机器人信息
     * @param botInfo bot实体类信息
     */
    @Override
    @Transactional
    public void update(BotInfo botInfo){
        botInfo.setPathSuffix(botInfo.getBotQQ().toString());
        botMapper.update(botInfo);
        if (botInfo.getPathSuffix() != null){
            botRegistrar.unregister(botInfo.getPathSuffix());
            botRegistrar.register(botInfo.getPathSuffix(),botInfo.getToken());
        }
    }

    /**
     * 查询机器人信息
     * @param userId 用户ID
     * @return 机器人信息
     */
    @Override
    public BotInfo select(String userId) {
        return botMapper.selectByUserId(userId);
    }

    /**
     * 获取机器人关联邮箱
     *
     * @param botQQ botQQ号
     * @return 机器人关联的邮箱
     */
    @Override
    public String selectEmail(Long botQQ) {
        return botMapper.selectEmail(botQQ);
    }

    /**
     * 获取所有机器人的路径与token
     *
     * @return 所有机器人的路径与token
     */
    @Override
    public List<Map<String, String>> selectPathSuffix() {
        return botMapper.selectPathSuffix();
    }

}
