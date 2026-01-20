package com.example.demo.mapper.task;

import com.example.demo.pojo.task.Receiver;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReceiverMapper {

    //todo:数据库及其相关层完成
    @Select("select * from bot")
    List<Receiver> getAll();
}
