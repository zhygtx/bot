package com.example.demo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Set;

@Mapper
public interface BotMapper {

    /**
     * 获取所有机器人QQ
     * @return 机器人QQ列表
     */
    @Select("SELECT bot_qq FROM bot")
    Set<Long> getAllBotQQs();

}
