package com.example.demo.core.manager;

import com.example.demo.pojo.msg.GroupMsg;
import com.example.demo.pojo.task.Role;

import java.util.List;

public interface ScopeManager {

    /**
     * 根据消息获取出发的作用域中所有的具体规则
     * @param groupMsg 消息对象
     * @return 触发作用域中匹配的规则
     */
    List<Role> getRoles(GroupMsg groupMsg);

}
