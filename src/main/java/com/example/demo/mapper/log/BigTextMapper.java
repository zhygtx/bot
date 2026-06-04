package com.example.demo.mapper.log;

import com.example.demo.pojo.entity.log.BigText;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BigTextMapper {

    void insertBatch(List<BigText> bigTextList);

    @Select("SELECT value FROM big_text WHERE `key` = #{key}")
    String selectByKey(String key);
}