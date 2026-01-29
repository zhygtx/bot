package com.example.demo.mapper.task.actionContent;

import com.example.demo.pojo.task.actionContent.Ai;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AiMapper {

    /**
     * 根据ID获取AI配置
     * @param id AI配置ID
     * @return AI配置
     */
    @Select("SELECT * FROM ai WHERE id = #{id}")
    Ai selectById(@Param("id") String id);

    /**
     * 根据用户ID获取AI配置列表
     * @param userId 用户ID
     * @return AI配置列表
     */
    @Select("SELECT * FROM ai WHERE user_id = #{userId}")
    List<Ai> selectByUserId(@Param("userId") String userId);

    /**
     * 插入AI配置
     * @param ai AI配置
     * @return 插入数量
     */
    @Insert("INSERT INTO ai (id, user_id, name, setting, api_key, compress_pct, model, net_search) VALUES (#{id}, #{userId}, #{name}, #{setting}, #{apiKey}, #{compressPct}, #{model}, #{netSearch})")
    int insert(Ai ai);

    /**
     * 更新AI配置
     * @param ai AI配置
     * @return 更新数量
     */
    @Update("UPDATE ai SET name = #{name}, setting = #{setting}, api_key = #{apiKey}, compress_pct = #{compressPct}, model = #{model}, net_search = #{netSearch} WHERE id = #{id}")
    int update(Ai ai);

    /**
     * 根据ID删除AI配置
     * @param id AI配置ID
     * @return 删除数量
     */
    @Delete("DELETE FROM ai WHERE id = #{id}")
    int deleteById(@Param("id") String id);

}