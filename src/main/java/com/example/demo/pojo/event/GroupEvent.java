package com.example.demo.pojo.event;

import com.example.demo.annotation.BotEvent;
import com.example.demo.annotation.EventField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * 群事件实体
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@BotEvent(
    type = "GroupChange",
    name = "群成员变动",
    description = "当群成员增加或减少时触发",
    order = 3
)
public class GroupEvent extends Event{

    /**
     * 被处理人ID
     */
    @EventField(description = "被处理人ID", order = 8)
    private Long userId;

    /**
     * 处理人ID
     */
    @EventField(description = "操作人的 QQ 号", order = 9)
    private Long operatorId;

}
