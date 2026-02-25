package com.example.demo.mapper;

import com.example.demo.pojo.plugin.PluginInfo;
import org.apache.ibatis.annotations.Mapper;

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
    int insertPlugin(PluginInfo pluginInfo);

    /**
     * 根据ID查询插件信息
     * @param id 插件ID
     * @return 插件信息
     */
    PluginInfo selectPluginById(String id);

    /**
     * 查询所有插件信息
     * @return 插件信息列表
     */
    List<PluginInfo> selectAllPlugins();

    /**
     * 更新插件信息
     * @param pluginInfo 插件信息
     */
    int updatePlugin(PluginInfo pluginInfo);

    /**
     * 删除插件信息
     * @param id 插件ID
     */
    int deletePlugin(String id);

    /**
     * 根据用户ID查询插件信息
     * @param authorId 作者ID
     * @return 插件信息列表
     */
    List<PluginInfo> selectPluginsByAuthorId(String authorId);

    /**
     * 查询公开插件
     * @return 插件信息列表
     */
    List<PluginInfo> selectPublicPlugins();
}
