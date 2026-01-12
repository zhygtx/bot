package com.example.demo.pojo;

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
     * 机器人是否在线
     */
    private Boolean isOnline;//todo: 是否在线
}
