package com.generalbot.bot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 机器人信息
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class BotInfo {

    /**
     * 机器人ID
     */
    private String id;

    /**
     * 机器人名称
     */
    private String name;

    /**
     * 机器人QQ
     */
    private Long botQQ;

    /**
     * 机器人所有者 ID
     */
    private String userId;

    /**
     * 机器人Token
     */
    private String token;

    /**
     * 机器人路径后缀
     */
    private String pathSuffix;

    /**
     * 机器人是否在线
     */
    private boolean isOnline = false;
}
