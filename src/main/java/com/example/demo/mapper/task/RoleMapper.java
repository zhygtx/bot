package com.example.demo.mapper.task;

import com.example.demo.pojo.task.Role;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RoleMapper {

    /**
     * 获取所有任务规则
     * @return 任务规则列表
     */
    @Select("SELECT * FROM role")
    List<Role> getAllRoles();

    /**
     * 根据ID获取任务规则
     * @param id 任务规则ID
     * @return 任务规则
     */
    @Select("SELECT * FROM role WHERE id = #{id}")
    Role selectById(@Param("id") String id);

    @Select("SELECT EXISTS(SELECT 1 FROM role WHERE md5 = #{md5})")
    Boolean existsByMd5(@Param("md5") String md5);

    /**
     * 根据用户ID获取任务规则列表
     * @param userId 用户ID
     * @return 任务规则列表
     */
    @Select("SELECT * FROM role WHERE user_id = #{userId}")
    List<Role> selectByUserId(@Param("userId") String userId);

    /**
     * 根据作用域ID获取任务规则列表
     * @param scopeId 作用域ID
     * @return 任务规则列表
     */
    @Select("SELECT * FROM role WHERE scope_id = #{scopeId}")
    List<Role> selectByScopeId(@Param("scopeId") String scopeId);

    /**
     * 插入任务规则
     * @param role 任务规则
     * @return 插入数量
     */
    @Insert("INSERT INTO role (id, user_id, scope_id, name ,md5, match_mode, regex, is_enable, is_extract) VALUES (#{id}, #{userId}, #{scopeId},#{name} ,#{MD5}, #{matchMode}, #{regex}, #{isEnable}, #{isExtract})")
    int insert(Role role);

    /**
     * 更新任务规则
     * @param role 任务规则
     * @return 更新数量
     */
    @Update("UPDATE role SET user_id = #{userId}, scope_id = #{scopeId},name=#{name} ,md5 = #{MD5}, match_mode = #{matchMode}, regex = #{regex}, is_enable = #{isEnable}, is_extract = #{isExtract} WHERE id = #{id}")
    int update(Role role);

    /**
     * 根据ID删除任务规则
     * @param id 任务规则ID
     * @return 删除数量
     */
    @Delete("DELETE FROM role WHERE id = #{id}")
    int deleteById(@Param("id") String id);

    /**
     * 根据作用域ID删除任务规则
     * @param scopeId 作用域ID
     * @return 删除数量
     */
    @Delete("DELETE FROM role WHERE scope_id = #{scopeId}")
    int deleteByScopeId(@Param("scopeId") String scopeId);

}
