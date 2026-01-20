package com.example.demo.mapper.task;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 提取位置Mapper
 */
@Mapper
public interface ExtractPositionMapper {

    /**
     * 获取作用域的提取位置
     * @return 角色ID与提取位置列表
     */
    @Select("SELECT role_id, extract_position FROM extract_position ORDER BY role_id")
    @MapKey("role_id")
    List<Map<String, Integer>> getAll();

    /**
     * 获取作用域的提取位置
     * @param roleId 角色ID
     * @return 提取位置列表
     */
    @Select("SELECT extract_position FROM extract_position WHERE role_id = #{roleId}")
    Set<Integer> getExtractPosition(String roleId);

    /**
     * 插入提取位置
     * @param extractPosition 提取位置
     * @return 插入数量
     */
    int insert(@Param("roleId") String roleId, @Param("extractPosition") Set<Integer> extractPosition);

    /**
     * 根据Role ID删除提取位置
     *
     * @param roleId Role ID
     */
    @Delete("DELETE FROM extract_position WHERE role_id = #{roleId}")
    void deleteByRoleId(@Param("roleId") String roleId);
}
