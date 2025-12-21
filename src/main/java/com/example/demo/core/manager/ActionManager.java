package com.example.demo.core.manager;

import com.example.demo.pojo.task.Action;

import java.util.List;
import java.util.Map;

public interface ActionManager {

    /**
     * 执行动作
     * @param actions 动作列表
     * @param msg 群消息对象
     * @return 执行结果
     */
    List<String> executeActions(Map<String,List<Action>> actions , Object msg);


}
