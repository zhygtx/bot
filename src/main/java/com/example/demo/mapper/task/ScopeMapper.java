package com.example.demo.mapper.task;

import com.example.demo.pojo.task.Scope;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ScopeMapper {

    /**
     * 获取所有作用域
     * @return 作用域列表
     */
    @Select("SELECT * FROM scope")
    List<Scope> getAllScopes();

    /**
     * 根据ID获取作用域
     * @param id 作用域ID
     * @return 作用域
     */
    @Select("SELECT * FROM scope WHERE id = #{id}")
    Scope selectById(@Param("id") String id);

    /**
     * 根据用户ID获取作用域列表
     * @param userId 用户ID
     * @return 作用域列表
     */
    @Select("SELECT * FROM scope WHERE user_id = #{userId}")
    List<Scope> selectByUserId(@Param("userId") String userId);

    /**
     * 插入作用域
     * @param scope 作用域
     * @return 插入数量
     */
    @Insert("INSERT INTO scope (id, name, user_id, bot_qq, is_at, qq_user_role, qq_user_id, qq_group_id, qq_bot_role, qq_scope_type, qq_scope_id) VALUES (#{id}, #{name}, #{userId}, #{botQQ}, #{isAt}, #{QQUserRole}, #{QQUserId}, #{QQGroupId}, #{QQBotRole}, #{QQScopeType}, #{QQScopeId})")
    int insert(Scope scope);

    /**
     * 更新作用域
     * @param scope 作用域
     * @return 更新数量
     */
    @Update("UPDATE scope SET name = #{name}, user_id = #{userId}, bot_qq = #{botQQ}, is_at = #{isAt}, qq_user_role = #{QQUserRole}, qq_user_id = #{QQUserId}, qq_group_id = #{QQGroupId}, qq_bot_role = #{QQBotRole}, qq_scope_type = #{QQScopeType}, qq_scope_id = #{QQScopeId} WHERE id = #{id}")
    int update(Scope scope);

    /**
     * 根据ID删除作用域
     * @param id 作用域ID
     * @return 删除数量
     */
    @Delete("DELETE FROM scope WHERE id = #{id}")
    int deleteById(@Param("id") String id);

}
