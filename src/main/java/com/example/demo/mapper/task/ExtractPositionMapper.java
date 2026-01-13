package com.example.demo.mapper.task;

import com.example.demo.pojo.task.ExtractPosition;
import org.apache.ibatis.annotations.*;

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

    /**
     * 插入提取位置
     * @param extractPosition 提取位置
     * @return 插入数量
     */
    @Insert("INSERT INTO extract_position (role_id, extract_position) VALUES (#{roleId}, #{extractPosition})")
    int insert(ExtractPosition extractPosition);

    /**
     * 更新提取位置
     * @param extractPosition 提取位置
     * @return 更新数量
     */
    @Update("UPDATE extract_position SET role_id = #{roleId}, extract_position = #{extractPosition} WHERE id = #{id}")
    int update(ExtractPosition extractPosition);

    /**
     * 根据ID删除提取位置
     * @param id 提取位置ID
     * @return 删除数量
     */
    @Delete("DELETE FROM extract_position WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 根据Role ID删除提取位置
     * @param roleId Role ID
     * @return 删除数量
     */
    @Delete("DELETE FROM extract_position WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") String roleId);

    /**
     * 根据ID获取提取位置
     * @param id 提取位置ID
     * @return 提取位置
     */
    @Select("SELECT * FROM extract_position WHERE id = #{id}")
    ExtractPosition selectById(@Param("id") Long id);

    /**
     * 根据Role ID获取提取位置列表
     * @param roleId Role ID
     * @return 提取位置列表
     */
    @Select("SELECT * FROM extract_position WHERE role_id = #{roleId}")
    List<ExtractPosition> selectByRoleId(@Param("roleId") String roleId);

}
