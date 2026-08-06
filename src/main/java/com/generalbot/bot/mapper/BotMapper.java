package com.generalbot.bot.mapper;

import com.generalbot.bot.entity.BotInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface BotMapper {

    /**
     * 更新机器人在线状态
     * @param botQQ 机器人QQ
     * @param online 是否在线
     */
    @Update("UPDATE bot SET is_online = #{online} WHERE bot_qq = #{botQQ}")
    void updateOnline(Long botQQ, Boolean online);

    /**
     * 插入机器人
     * @param botInfo 机器人
     */
    @Insert("INSERT INTO bot (id,bot_qq, name, user_id,token, path_suffix) " +
            "VALUES (#{id},#{botQQ}, #{name}, #{userId},#{token}, #{pathSuffix})")
    void insert(BotInfo botInfo);

    /**
     * 删除机器人
     * @param botQQ botQQ
     */
    @Delete("DELETE FROM bot WHERE bot_qq = #{botQQ}")
    void delete(Long botQQ);

    /**
     * 更新机器人信息
     * @param botInfo 机器人
     */
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

    @Select("SELECT * FROM bot WHERE bot_qq = #{botQQ}")
    BotInfo selectByBotQQ(Long botQQ);

    @Select("select email from user where id = (select user_id from bot where bot_qq = #{botQQ})")
    String selectEmail(Long botQQ);

    @Select("select path_suffix , token from bot")
    List<Map<String,String>> selectPathSuffix();
}
