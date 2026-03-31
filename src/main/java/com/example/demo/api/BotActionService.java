package com.example.demo.api;

import java.util.Map;

public interface BotActionService {

    /**
     * 发送群消息
     * @param botQQ 机器人QQ
     * @param groupId 群号
     * @param msg 消息
     */
    void sendGroupMsg(Long botQQ,Long groupId, String msg);

    /**
     * 发送私聊消息
     * @param botQQ 机器人QQ
     * @param userId 用户QQ
     * @param msg 消息
     */
    void sendPrivateMsg(Long botQQ, Long userId, String msg);

    /**
     * 批量发送群消息
     * @param msg 群消息, key为群号, value为消息内容
     */
    void sendGroupMsgBatch(Long botQQ, Map<Long, String> msg);

    /**
     * 批量发送私聊消息
     * @param msg 私聊消息, key为用户QQ, value为消息内容
     */
    void sendPrivateMsgBatch(Long botQQ, Map<Long, String> msg);
}
