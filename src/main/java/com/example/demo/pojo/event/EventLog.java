package com.example.demo.pojo.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class EventLog {

    /**
     *  机器人QQ
     */
    private Long selfId;

    /**
     *  事件类型
     */
    private String type;

    /**
     *  事件子类型
     */
    private String subType;

    /**
     *  事件时间
     */
    private Long time;

    /**
     *  事件触发QQ
     */
    private Long userId;

    /**
     *  事件触发群
     */
    private Long groupId;

    /**
     *  消息类型
     */
    private String msgType;

    /**
     *  事件数据源
     */
    private String eventData;
}
