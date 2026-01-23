package com.example.demo.pojo.event;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 群消息实体
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class GroupMsg extends Msg{

    /**
     * 是否@了Bot
     */
    @Builder.Default
    private boolean isAt = false;

    /**
     * 发送者用户权限
     */
    private String userRole;
}
