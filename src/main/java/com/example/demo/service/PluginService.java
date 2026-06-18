package com.example.demo.service;

import com.example.demo.pojo.dto.PluginInfoDto;
import com.example.demo.pojo.entity.Result;
import com.example.demo.pojo.entity.plugin.PluginInfo;
import com.github.pagehelper.PageInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface PluginService {

    /**
     * 添加插件
     * @param pluginInfo 插件信息
     * @param file 插件文件
     * @return 添加结果
     */
    Result<?> add(PluginInfo pluginInfo, MultipartFile file);

   /**
     * 删除插件
     * @param id 插件id
     * @return 删除结果
     */
    int remove(String id);

    /**
     * 修改插件信息
     * @param pluginInfo 插件信息
     * @return 修改结果
     */
    int edit(PluginInfo pluginInfo);

    /**
     * 修改插件信息
     * @param id 插件id
     * @param isPublic 是否公开
     * @return 修改结果
     */
    int editPublic(String id, boolean isPublic);

    /**
     * 根据id和versionId查询插件信息
     * @param id 插件id
     * @param versionId 插件版本id
     * @return 插件信息
     */
    PluginInfo findByPluginIdAndVersionId(String id, String versionId);

    /**
     * 获取插件列表
     * @param content 模糊查询内容
     * @param authorId 插件作者id
     * @param isPublic 插件是否公开
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 插件列表
     */
    PageInfo<PluginInfoDto> findPlugins(String content, String authorId, Boolean isPublic, int pageNum, int pageSize);

    /**
     * 获取插件版本列表
     * @param pluginId 插件id
     * @return 插件版本列表
     */
    List<Map<String, Object>> findPluginVersionByPluginId(String pluginId);
}
