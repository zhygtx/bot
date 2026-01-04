package com.example.demo.mapper.task;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 提取位置Mapper
 */
@Mapper
public interface ExtractPositionMapper {

    /**
     * 获取作用域的提取位置
      * @param roleId 角色ID
     * @return 提取位置列表
     */
    @Select("SELECT extract_position FROM extract_position WHERE role_id = #{roleId}")
    List<Integer> getExtractPosition(String roleId);

}
