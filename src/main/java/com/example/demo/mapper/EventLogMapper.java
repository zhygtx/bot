package com.example.demo.mapper;

import com.example.demo.pojo.event.EventLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EventLogMapper {

    /**
     *  批量插入事件
     *  @param events 事件列表
     */
    void insert(@Param("events") List<EventLog> events);

    /**
     *  根据群组和类型查询事件
     *  @param group 群组ID
     *  @param type 事件类型
     */
    @Select("SELECT * FROM event_log WHERE group_id = #{group} AND type = #{type}")
    List<EventLog> getByGroupAndType(Long group, String type);
}
