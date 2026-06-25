package com.example.demo.service.impl;

import com.example.demo.mapper.BotMapper;
import com.example.demo.pojo.entity.BotInfo;
import com.example.demo.pojo.entity.Result;
import com.example.demo.service.BotService;
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
     * 更新机器人是否在线
     * @param botQQ 机器人QQ
     * @param online 是否在线
     */
    @Override
    @Transactional
    public void updateOnline(Long botQQ, boolean online){
        botMapper.updateOnline(botQQ, online);
    }

    /**
     * 插入机器人
     * @param name 机器人名称
     * @param botQQ 机器人QQ
     */
    @Override
    @Transactional
    public Result<?> insert(String userId, String name, Long botQQ) {
        if (botMapper.existsByBotQQ(botQQ)) {
            return Result.error(400,"该bot已被注册");
        }
        BotInfo bot = new BotInfo();
        bot.setId(UUID.randomUUID().toString());
        bot.setBotQQ(botQQ);
        bot.setName(name);
        bot.setUserId(userId);
        bot.setPathSuffix(botQQ.toString());
        bot.setToken(UUID.randomUUID().toString());
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
        botMapper.update(botInfo);
        if (botInfo.getPathSuffix() != null){
            botRegistrar.updateToken(botInfo.getPathSuffix(),botInfo.getToken());
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