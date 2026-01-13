package com.example.demo.mapper.task.actionContent;

import com.example.demo.pojo.task.actionContent.Api;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ApiMapper {

    /**
     * 根据ID获取API动作细节
     * @param id 动作ID
     * @return API动作细节
     */
    @Select("SELECT * FROM api WHERE id = #{id}")
    Api getApi(String id);

    /**
     * 获取所有API调用内容
     * @return API调用内容列表
     */
    @Select("SELECT * FROM api")
    List<Api> getAllApis();

    /**
     * 根据ID获取API调用内容
     * @param id API调用内容ID
     * @return API调用内容
     */
    @Select("SELECT * FROM api WHERE id = #{id}")
    Api selectById(@Param("id") String id);

    /**
     * 根据用户ID获取API调用内容列表
     * @param userId 用户ID
     * @return API调用内容列表
     */
    @Select("SELECT * FROM api WHERE user_id = #{userId}")
    List<Api> selectByUserId(@Param("userId") String userId);

    /**
     * 插入API调用内容
     * @param api API调用内容
     * @return 插入数量
     */
    @Insert("INSERT INTO api (id, user_id, name) VALUES (#{id}, #{userId}, #{name})")
    int insert(Api api);

    /**
     * 更新API调用内容
     * @param api API调用内容
     * @return 更新数量
     */
    @Update("UPDATE api SET user_id = #{userId}, name = #{name} WHERE id = #{id}")
    int update(Api api);

    /**
     * 根据ID删除API调用内容
     * @param id API调用内容ID
     * @return 删除数量
     */
    @Delete("DELETE FROM api WHERE id = #{id}")
    int deleteById(@Param("id") String id);

}
