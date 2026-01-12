package com.example.demo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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
}
