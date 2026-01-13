package com.example.demo.mapper;

import com.example.demo.pojo.BotInfo;
import org.apache.ibatis.annotations.*;

import java.util.Set;

@Mapper
public interface BotMapper {

    /**
     * 获取所有机器人QQ
     * @return 机器人QQ列表
     */
    @Select("SELECT bot_qq FROM bot")
    Set<Long> getAllBotQQs();

    /**
     * 更新机器人在线状态
     * @param botQQ 机器人QQ
     * @param online 是否在线
     */
    @Update("UPDATE bot SET online = #{online} WHERE bot_qq = #{botQQ}")
    void updateOnline(Long botQQ, Boolean online);

    /**
     * 插入机器人
     * @param botInfo 机器人
     */
    @Insert("INSERT INTO bot (id,bot_qq, name, user_id) " +
            "VALUES (#{id},#{botQQ}, #{name}, #{userId})")
    void insert(BotInfo botInfo);

    /**
     * 删除机器人
     * @param userId 用户ID
     */
    @Delete("DELETE FROM bot WHERE user_id = #{userId}")
    void delete(String userId);

    /**
     * 更新机器人信息
     * @param botInfo 机器人
     */
    @Update("UPDATE bot SET name = #{name}, bot_qq = #{botQQ} WHERE id = #{id}")
    void update(BotInfo botInfo);

    /**
     * 获取机器人信息
     * @param userId 用户ID
     * @return 机器人信息
     */
    @Select("SELECT * FROM bot WHERE user_id = #{userId}")
    BotInfo selectByUserId(String userId);

    @Select("SELECT EXISTS(SELECT * FROM bot WHERE bot_qq = #{botQQ})")
    boolean existsByBotQQ(Long botQQ);
}
