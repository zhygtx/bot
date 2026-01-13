package com.example.demo.service.impl;

import com.example.demo.mapper.BotMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.pojo.BotInfo;
import com.example.demo.pojo.Result;
import com.example.demo.service.BotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

/**
 * 机器人服务实现类
 */
@Service
public class BotServiceImpl implements BotService {

    private final BotMapper botMapper;
    private final UserMapper userMapper;

    @Autowired
    public BotServiceImpl(BotMapper botMapper, UserMapper userMapper) {
        this.botMapper = botMapper;
        this.userMapper = userMapper;
    }

    /**
     * 获取所有机器人QQ
     * @return 机器人QQ列表
     */
    @Override
    public Set<Long> getAllBotQQs() {
        return botMapper.getAllBotQQs();
    }

    /**
     * 更新机器人是否在线
     * @param botQQ 机器人QQ
     * @param online 是否在线
     */
    @Override
    public void updateOnline(Long botQQ, boolean online){
        botMapper.updateOnline(botQQ, online);
    }

    /**
     * 插入机器人
     * @param name 机器人名称
     * @param botQQ 机器人QQ
     */
    @Override
    public Result<?> insert(String userId, String name, Long botQQ) {
        if (botMapper.existsByBotQQ(botQQ)) {
            return Result.error("该bot已被注册");
        }
        BotInfo bot = new BotInfo();
        bot.setId(UUID.randomUUID().toString());
        bot.setBotQQ(botQQ);
        bot.setName(name);
        bot.setUserId(userId);
        userMapper.updateBotQQ(userId, botQQ);
        botMapper.insert(bot);
        return Result.success();
    }

    /**
     * 删除机器人
      * @param userId 用户ID
     */
    @Override
    public void delete(String userId){
        userMapper.updateBotQQ(userId, null);
        botMapper.delete(userId);
    }

    /**
     * 更新机器人信息
     * @param userId 用户ID
     * @param name 机器人名称
     * @param botQQ 机器人QQ
     */
    @Override
    public void update(String userId, String name, Long botQQ){
        BotInfo bot = botMapper.selectByUserId(userId);
        bot.setName(name);
        bot.setBotQQ(botQQ);
        botMapper.update(bot);
        userMapper.updateBotQQ(userId, botQQ);
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

}
