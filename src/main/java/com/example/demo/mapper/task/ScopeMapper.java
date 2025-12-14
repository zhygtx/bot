package com.example.demo.mapper.task;

import com.example.demo.pojo.task.Scope;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ScopeMapper {

    /**
     * 获取所有作用域
     * @return 作用域列表
     */
    @Select("SELECT * FROM scope")
    List<Scope> getAllScopes();

}
