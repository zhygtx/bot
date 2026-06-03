package com.example.demo.mapper.plugin;

import com.example.demo.pojo.entity.plugin.PluginVersion;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 插件版本信息映射接口
 */
@Mapper
public interface PluginVersionMapper {

    /**
     * 批量插入插件版本信息
     * @param pluginVersion 插件版本信息
     * @return 插入结果
     */
    int insert(PluginVersion pluginVersion);

    /**
     * 批量更新插件版本信息
     * @param pluginVersions 插件版本信息
     * @return 更新结果
     */
    int update(List<PluginVersion> pluginVersions);
}
