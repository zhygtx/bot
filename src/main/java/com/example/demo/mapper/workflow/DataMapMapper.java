package com.example.demo.mapper.workflow;

import com.example.demo.pojo.workflow.DataMap;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 数据映射Mapper
 */
@Mapper
public interface DataMapMapper {

    /**
     * 批量插入数据映射
     * @param dataMaps 数据映射列表
     * @return 插入数量
     */
    int insert(List<DataMap> dataMaps);
}
