package com.example.demo.core.manager;

import com.example.demo.pojo.task.Action;
import com.example.demo.pojo.task.Receiver;

import java.util.List;
import java.util.Map;

public interface ActionManager {

    /**
     * 执行动作
     * @param actions 动作列表
     * @param msg 群消息对象
     * @return <消息类型<群号/qq号，消息内容>>执行结果
     */
    Map<Receiver.ReceiverType, Map<Long,String>> executeActions(List<Action> actions , Object msg);


}
