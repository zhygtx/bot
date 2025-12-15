package com.example.demo.mapper.task.actionContent;

import com.example.demo.pojo.task.actionContent.Api;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ApiMapper {

    /**
     * 根据ID获取API动作细节
     * @param id 动作ID
     * @return API动作细节
     */
    @Select("SELECT * FROM api WHERE id = #{id}")
    Api getApi(String id);

}
