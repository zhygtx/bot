package com.example.demo.mapper.workflow;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 节点前置关系Mapper
 */
@Mapper
public interface NodePreRelationMapper {

    /**
     * 批量插入节点前置关系
     * @param map 节点前置关系列表
     * @return 插入数量
     */
    int insert(Map<String, List<String>> map);
}
