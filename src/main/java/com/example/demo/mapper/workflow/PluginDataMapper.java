package com.example.demo.mapper.workflow;

import com.github.zhygtx.pojo.PluginData;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface PluginDataMapper {

    @Insert("insert into plugin_data(data,user_id,plugin_id,create_time,update_time) values(#{data},#{userId},#{pluginId},now(),now())")
    int insertByData(String data, String userId, String pluginId);

    @Insert("insert into plugin_data(data_index,data,user_id,plugin_id,create_time,update_time) values(#{index},#{data},#{userId},#{pluginId},now(),now())")
    int insertByIndexData(String index, String data, String userId, String pluginId);

    int insertByList(@Param("index") String index, @Param("data") List<String> data, @Param("userId") String userId, @Param("pluginId") String pluginId);

    int insertByMap(@Param("data") Map<String, String> data, @Param("userId") String userId, @Param("pluginId") String pluginId);

    int insertByMapList(@Param("data") Map<String,List<String>> data, @Param("userId") String userId, @Param("pluginId") String pluginId);

    @Delete("delete from plugin_data where user_id = #{userId} and plugin_id = #{pluginId}")
    int delete(String userId, String pluginId);

    @Delete("delete from plugin_data where id = #{id}")
    int deleteById(Integer id);

    @Delete("delete from plugin_data where data_index = #{index} and user_id = #{userId} and plugin_id = #{pluginId}")
    int deleteByIndex(String index, String userId, String pluginId);

    int deleteByIndexList(@Param("index") List<String> index, @Param("userId") String userId, @Param("pluginId") String pluginId);

    int deleteByIds(@Param("ids") List<Integer> ids);

    @Update("update plugin_data set data = #{data}, update_time = now() where id = #{id}")
    int updateById(Integer id, String data);

    int updateByIndex(@Param("index") String index, @Param("data") String data, @Param("userId") String userId, @Param("pluginId") String pluginId);

    @Select("select * from plugin_data where user_id = #{userId} and plugin_id = #{pluginId}")
    List<PluginData> selectAll(String userId, String pluginId);

    @Select("select * from plugin_data where id = #{id}")
    PluginData selectById(Integer id);

    @Select("select * from plugin_data where data_index = #{index} and user_id = #{userId} and plugin_id = #{pluginId}")
    List<PluginData> selectByIndex(String index, String userId, String pluginId);
}
