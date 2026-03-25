package com.example.demo.api;

public interface BotActionService {

    void sendGroupMsg(Long botId,Long groupId, String msg);

    void sendPrivateMsg(Long botId,Long userId, String msg);

}
