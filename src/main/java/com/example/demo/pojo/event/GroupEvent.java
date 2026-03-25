package com.example.demo.pojo.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * 群事件实体
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class GroupEvent extends Event{

    /**
     * 被处理人ID
     */
    private Long userId;

    /**
     * 处理人ID
     */
    private Long operatorId;

}
