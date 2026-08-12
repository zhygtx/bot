package com.generalbot.bot.mapper;

import com.generalbot.bot.entity.BotInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface BotMapper {

    /**
     * 更新机器人在线状态，同时记录本次上线时间。
     * 上线时传入当前时间戳，离线时传入 null 把 online_since 清空，
     * 这样统计首页可以通过“当前时间 - online_since”得到本次在线时长。
     * @param botQQ 机器人QQ
     * @param online 是否在线
     * @param onlineSince 本次上线时间戳（毫秒），离线时为 null
     */
    @Update("UPDATE bot SET is_online = #{online}, online_since = #{onlineSince} WHERE bot_qq = #{botQQ}")
    void updateOnline(@Param("botQQ") Long botQQ, @Param("online") Boolean online, @Param("onlineSince") Long onlineSince);

    @Update("UPDATE bot SET is_online = false, online_since = NULL")
    void markAllOffline();

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
