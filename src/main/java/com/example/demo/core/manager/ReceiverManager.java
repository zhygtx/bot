package com.example.demo.core.manager;

import com.example.demo.pojo.task.Receiver;
import com.mikuac.shiro.core.Bot;

import java.util.Map;

public interface ReceiverManager {

    /**
     * 发送消息
     * @param bot 机器人对象
     * @param receivers 接收对象
     */
    void sendMessage(Bot bot, Map<Receiver.ReceiverType, Map<Long,String>> receivers);

}
