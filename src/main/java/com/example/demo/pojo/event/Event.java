package com.example.demo.pojo.event;

import com.example.demo.annotation.BotEvent;
import com.example.demo.annotation.EventField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@BotEvent(
    type = "BaseEvent",
    name = "基础事件",
    description = "所有事件的基类",
    order = 0
)
public class Event {

    /**
     * 接收信息的BotID
     */
    @EventField(description = "BOT 的 QQ 号", order = 1, inherited = true)
    private Long botId;

    /**
     * 发送者ID
     */
    @EventField(description = "用户的 QQ 号", order = 2, inherited = true)
    private Long userId;

    /**
     * 群ID
     */
    @EventField(description = "群号", order = 3, inherited = true)
    private Long groupId;

    /**
     * 消息类型
     */
    @EventField(description = "事件类型", order = 4, inherited = true)
    private Type eventType;

    /**
     * 事件原本数据
     */
    @EventField(description = "事件原本数据", order = 5, inherited = true)
    private Object data;

    /**
     * 事件类型枚举
     */
    public enum Type {
        GroupMsg,//群消息
        PrivateMsg,//私聊消息
        GroupIncrease,//群成员增加
        GroupDecrease,//退群事件
        GroupAddRequest//群加请求
    }
}