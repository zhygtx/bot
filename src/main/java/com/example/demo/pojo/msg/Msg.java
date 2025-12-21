package com.example.demo.pojo.msg;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

@Data
@SuperBuilder
public class Msg {

    /**
     * 发送者ID
     */
    private Long userId;

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
