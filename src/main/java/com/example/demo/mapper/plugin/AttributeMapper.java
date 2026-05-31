package com.example.demo.mapper.plugin;

import com.example.demo.pojo.plugin.Attribute;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 属性信息映射接口
 */
@Mapper
public interface AttributeMapper {

    /**
     * 批量插入属性信息
     * @param attributeList 属性信息列表
     * @return 插入数量
     */
    int insert(List<Attribute> attributeList);

    /**
     * 批量更新属性信息
     * @param attributeList 属性信息列表
     * @return 更新数量
     */
    int update(List<Attribute> attributeList);
}
