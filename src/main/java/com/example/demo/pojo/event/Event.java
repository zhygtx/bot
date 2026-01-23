package com.example.demo.pojo.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Event {

    /**
     * 接收信息的BotID
     */
    private Long botId;


    /**
     * 发送者ID
     */
    private Long userId;

    /**
     * 群ID
     */
    private Long groupId;

    /**
     * 消息类型
     */
    private Type eventType;

    /**
     * 事件原本数据
     */
    private Object data;

    /**
     * 事件类型枚举
     */
    public enum Type {
        GroupMsg,//群消息
        PrivateMsg,//私聊消息
        GroupIncrease,//群成员增加
        GroupDecrease//退群事件
    }
}