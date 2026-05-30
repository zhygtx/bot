package com.example.demo.pojo.event;

import com.example.demo.annotation.BotEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * 私聊消息实体
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@BotEvent(
    type = "PrivateMsg",
    name = "私聊消息",
    description = "当 BOT 收到私聊消息时触发",
    order = 2
)
public class PrivateMsg extends Msg{

}
