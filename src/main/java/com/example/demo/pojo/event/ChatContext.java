package com.example.demo.pojo.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天上下文
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ChatContext {

    /**
     * 上下文ID
     */
    private String id;

    /**
     * BotID
     */
    private Long botId;

    /**
     * 发送者类型
     */
    private String role;

    /**
     * 群ID
     */
    private Long groupId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 消息类型
     */
    private String msgType;

    /**
     * 消息内容
     */
    private String msg;

    /**
     * 是否为上下文总结
     */
    private Boolean isSummary;

    /**
     * 上下文总结Id
     */
    private String summaryId;

    /**
     * 此次使用的token
     */
    private Integer useToken;

    /**
     * 时间戳
     */
    private Long time;
}
