package com.example.demo.mapper;

import com.example.demo.pojo.event.EventLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EventLogMapper {

    /**
     *  批量插入事件
     *  @param events 事件列表
     */
    void insert(@Param("events") List<EventLog> events);
}
