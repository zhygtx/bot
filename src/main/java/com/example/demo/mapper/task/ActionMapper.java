package com.example.demo.mapper.task;

import com.example.demo.pojo.task.Action;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ActionMapper {

    /**
     * 根据规则ID获取动作列表
     * @param roleId 规则ID
     * @return 动作列表
     */
    @Select("SELECT * FROM action WHERE role_id = #{roleId}")
    List<Action> getActions(String roleId);

    /**
     * 根据ID获取动作
     * @param id 动作ID
     * @return 动作
     */
    @Select("SELECT * FROM action WHERE id = #{id}")
    Action selectById(@Param("id") String id);

    /**
     * 根据用户ID获取动作列表
     * @param userId 用户ID
     * @return 动作列表
     */
    @Select("SELECT * FROM action WHERE user_id = #{userId}")
    List<Action> selectByUserId(@Param("userId") String userId);

    /**
     * 根据规则ID获取动作列表
     * @param roleId 规则ID
     * @return 动作列表
     */
    @Select("SELECT * FROM action WHERE role_id = #{roleId}")
    List<Action> selectByRoleId(@Param("roleId") String roleId);

    /**
     * 插入动作
     * @param action 动作
     * @return 插入数量
     */
    @Insert("INSERT INTO action (id, role_id, user_id, need_at, action_type, data_id, is_concat, seq) VALUES (#{id}, #{roleId}, #{userId}, #{needAt}, #{actionType}, #{dataId}, #{isConcat}, #{seq})")
    int insert(Action action);

    /**
     * 更新动作
     * @param action 动作
     * @return 更新数量
     */
    @Update("UPDATE action SET role_id = #{roleId}, user_id = #{userId}, need_at = #{needAt}, action_type = #{actionType}, data_id = #{dataId}, is_concat = #{isConcat}, seq = #{seq} WHERE id = #{id}")
    int update(Action action);

    /**
     * 根据ID删除动作
     * @param id 动作ID
     * @return 删除数量
     */
    @Delete("DELETE FROM action WHERE id = #{id}")
    int deleteById(@Param("id") String id);

    /**
     * 根据规则ID删除动作
     * @param roleId 规则ID
     * @return 删除数量
     */
    @Delete("DELETE FROM action WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") String roleId);

}
