package com.example.demo.service;

import com.example.demo.pojo.event.EventLog;

import java.util.List;

public interface EventLogService {

    /**
     *  插入事件日志
     * @param event 事件对象
     */
    void insert(EventLog event);

    /**
     *  根据群组和类型获取事件日志
     * @param group 群组ID
     * @param type 日志类型
     * @return 事件日志列表
     */
    List<EventLog> getEventLogs(Long group, String type);

    /**
     *  定时任务：每分钟检查并处理日志
     */
    @SuppressWarnings("unused")
    void scheduledProcess();
}
