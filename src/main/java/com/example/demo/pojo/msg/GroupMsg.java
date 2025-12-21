package com.example.demo.pojo.msg;

import lombok.*;

/**
 * 群消息实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GroupMsg extends Msg{

    /**
     * 群ID
     */
    private Long groupId;

    /**
     * 是否@了Bot
     */
    private boolean isAt = false;

    /**
     * 发送者用户权限
     */
    private String userRole;
}
