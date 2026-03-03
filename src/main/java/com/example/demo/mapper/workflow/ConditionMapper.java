package com.example.demo.mapper.workflow;

import com.example.demo.pojo.workflow.Condition;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 条件映射类
 */
@Mapper
public interface ConditionMapper {

    /**
     * 批量插入条件
     * @param conditions 条件列表
     * @return 插入数量
     */
    int insert(List<Condition> conditions);
}
