package com.example.demo.mapper.task;

import com.example.demo.pojo.task.Receiver;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReceiverMapper {

    /**
     * 获取所有接收对象
     * @return 接收对象列表
     */
    @Select("select * from receiver")
    List<Receiver> getAll();

    /**
     * 根据动作ID获取接收对象列表
     * @param actionId 动作ID
     * @return 接收对象列表
     */
    @Select("select * from receiver where action_id = #{actionId}")
    List<Receiver> getByActionId(String actionId);

    /**
     * 批量插入
     * @param receivers 接收对象列表
     */
    void insert(@Param("list") List<Receiver> receivers);

    /**
     * 批量删除
     * @param actionId 动作ID
     */
    @Delete("delete from receiver where action_id = #{actionId}")
    void deleteByActionId(String actionId);
}
