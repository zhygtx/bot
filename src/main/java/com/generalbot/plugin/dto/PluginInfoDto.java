package com.generalbot.plugin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 插件信息DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PluginInfoDto {

    /**
     * 插件id
     */
    private String id;

    /**
     * 插件名称
     */
    private String name;

    /**
     * 插件描述
     */
    private String description;

    /**
     * 插件作者ID
     */
    private String authorId;

    /**
     * 插件作者名称
     */
    private String authorName;

    /**
     * 最新版本号
     */
    private String latestVersion;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否公开
     */
    private Boolean isPublic;

    /**
     * 插件版本信息列表
     */
    private List<PluginVersionDto> pluginVersionList;
}
