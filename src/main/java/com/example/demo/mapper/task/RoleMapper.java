package com.example.demo.mapper.task;

import com.example.demo.pojo.task.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMapper {

    /**
     * 获取所有任务规则
     * @return 任务规则列表
     */
    @Select("SELECT * FROM role")
    List<Role> getAllRoles();

}
