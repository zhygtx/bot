package com.example.demo.mapper.task;

import com.example.demo.pojo.task.Action;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ActionMapper {

    /**
     * 根据规则ID获取动作列表
     * @param roleId 规则ID
     * @return 动作列表
     */
    @Select("SELECT * FROM action WHERE role_id = #{roleId}")
    List<Action> getActions(String roleId);

}
