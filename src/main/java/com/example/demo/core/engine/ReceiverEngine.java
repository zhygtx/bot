package com.example.demo.core.engine;

import com.example.demo.core.manager.ReceiverManager;
import com.example.demo.pojo.task.Receiver;
import com.mikuac.shiro.core.Bot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 接收对象处理类
 */
@Slf4j
@Component
public class ReceiverEngine implements ReceiverManager {

    /**
     * 发送消息
     * @param bot 机器人对象
     * @param receivers 接收对象
     */
    @Override
    public void sendMessage(Bot bot, Map<Receiver.ReceiverType, Map<Long,String>> receivers) {
        if (!receivers.isEmpty()){
            receivers.forEach((receiverType, receiverMap) ->
                    receiverMap.forEach((receiverQQ, message) -> {
                        switch (receiverType) {
                            case Private:
                                bot.sendPrivateMsg(receiverQQ, message,false);
                                break;
                            case Group:
                                bot.sendGroupMsg(receiverQQ, message, false);
                                break;
                        }
                    }));
        }
    }

}
