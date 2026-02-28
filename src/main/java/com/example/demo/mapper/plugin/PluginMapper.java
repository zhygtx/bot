package com.example.demo.mapper.plugin;

import com.example.demo.pojo.plugin.PluginInfo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 插件信息映射接口
 */
@Mapper
public interface PluginMapper {

    /**
     * 插入插件信息
     * @param pluginInfo 插件信息
     */
    int insert(PluginInfo pluginInfo);

    /**
     * 删除插件信息
     * @param id 插件ID
     */
    @Delete("delete from plugin_info where id = #{id}")
    int delete(String id);

    /**
     * 更新插件信息
     * @param pluginInfo 插件信息
     */
    int update(PluginInfo pluginInfo);

    @Select("select exists(select * from plugin_info where author_id = #{authorId} and name = #{name})")
    Boolean existsByAuthorIdAndName(String authorId, String name);

    /**
     * 根据ID查询插件信息
     * @param id 插件ID
     * @return 插件信息
     */
    PluginInfo selectById(String id);

    /**
     * 查询所有插件信息
     * @return 插件信息列表
     */
    List<PluginInfo> selectAll();

    /**
     * 根据用户ID查询插件信息
     * @param authorId 作者ID
     * @return 插件信息列表
     */
    List<PluginInfo> selectByAuthorId(String authorId);

    /**
     * 查询公开插件
     * @return 插件信息列表
     */
    List<PluginInfo> selectByPublic();
}
