package com.example.demo.service;

import com.example.demo.pojo.event.EventLog;

public interface EventLogService {

    /**
     *  插入事件日志
     * @param event 事件对象
     */
    void insert(EventLog event);

    /**
     *  定时任务：每分钟检查并处理日志
     */
    void scheduledProcess();
}
