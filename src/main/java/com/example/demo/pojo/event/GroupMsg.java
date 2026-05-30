package com.example.demo.pojo.event;

import com.example.demo.annotation.BotEvent;
import com.example.demo.annotation.EventField;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * 群消息实体
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@BotEvent(
    type = "GroupMsg",
    name = "群消息",
    description = "当 BOT 收到群消息时触发",
    order = 1
)
public class GroupMsg extends Msg{

    /**
     * 是否@了Bot
     */
    @EventField(description = "是否@了BOT", order = 8)
    @Builder.Default
    private boolean isAt = false;

    /**
     * 发送者用户权限
     */
    @EventField(description = "发送者用户权限", order = 9)
    private String userRole;
}
