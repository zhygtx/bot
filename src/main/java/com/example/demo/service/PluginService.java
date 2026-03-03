package com.example.demo.service;

import com.example.demo.pojo.Result;
import com.example.demo.pojo.plugin.PluginInfo;
import com.github.pagehelper.PageInfo;
import org.springframework.web.multipart.MultipartFile;

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
     * 获取插件列表
     * @param authorId 插件作者ID
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 插件列表
     */
    PageInfo<PluginInfo> findByAuthorId(String authorId, int pageNum, int pageSize);

    /**
     * 根据id查询插件信息
     * @param id 插件id
     * @return 插件信息
     */
    PluginInfo findById(String id);
}
