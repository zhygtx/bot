package com.example.demo.api;

import com.example.demo.annotation.BotAction;
import com.example.demo.annotation.ActionParam;

import java.util.Map;

public interface BotActionService {

    /**
     * 发送群消息
     * @param botQQ 机器人QQ
     * @param groupId 群号
     * @param msg 消息
     */
    @BotAction(
        name = "发送群消息",
        description = "向指定群发送消息",
        order = 1
    )
    void sendGroupMsg(
        @ActionParam(description = "BOT 的 QQ 号", order = 1) Long botQQ,
        @ActionParam(description = "群号", order = 2) Long groupId,
        @ActionParam(description = "消息内容", order = 3) String msg
    );

    /**
     * 发送私聊消息
     * @param botQQ 机器人QQ
     * @param userId 用户QQ
     * @param msg 消息
     */
    @BotAction(
        name = "发送私聊消息",
        description = "向指定用户发送私聊消息",
        order = 2
    )
    void sendPrivateMsg(
        @ActionParam(description = "BOT 的 QQ 号", order = 1) Long botQQ,
        @ActionParam(description = "用户的 QQ 号", order = 2) Long userId,
        @ActionParam(description = "消息内容", order = 3) String msg
    );

    /**
     * 批量发送群消息
     * @param msg 群消息, key为群号, value为消息内容
     */
    @BotAction(
        name = "批量发送群消息",
        description = "批量向多个群发送消息",
        order = 3
    )
    void sendGroupMsgBatch(
        @ActionParam(description = "BOT 的 QQ 号", order = 1) Long botQQ,
        @ActionParam(description = "群消息，key 为群号，value 为消息内容", order = 2) Map<Long, String> msg
    );

    /**
     * 批量发送私聊消息
     * @param msg 私聊消息, key为用户QQ, value为消息内容
     */
    @BotAction(
        name = "批量发送私聊消息",
        description = "批量向多个用户发送私聊消息",
        order = 4
    )
    void sendPrivateMsgBatch(
        @ActionParam(description = "BOT 的 QQ 号", order = 1) Long botQQ,
        @ActionParam(description = "私聊消息，key 为用户 QQ，value 为消息内容", order = 2) Map<Long, String> msg
    );
}
