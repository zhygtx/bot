package com.example.demo.api;

public interface BotActionService {

    void sendGroupMsg(Long botQQ,Long groupId, String msg);

    void sendPrivateMsg(Long botQQ,Long userId, String msg);

}
