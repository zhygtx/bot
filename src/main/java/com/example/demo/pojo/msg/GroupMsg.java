package com.example.demo.pojo.msg;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 群消息实体
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class GroupMsg {

    /**
     * 群ID
     */
    private Long groupId;

    /**
     * 发送者ID
     */
    private Long userId;

    /**
     * 是否@了Bot
     */
    private boolean isAt = false;

    /**
     * 发送者用户权限
     */
    private String userRole;

    /**
     * 接收信息的BotID
     */
    private Long botId;

    /**
     * 消息类型
     */
    private List<String> type;

    /**
     * 群消息内容
     */
    private Map<Integer, Map<String, Object>> content;
}
