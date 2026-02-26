package com.example.demo.pojo.plugin;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PluginInfo {

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
     * 插件作者（即userId）
     */
    private String authorId;

    /**
     * 当前版本号（方便查询）
     */
    private String currentVersion;

    /**
     * 最新版本号
     */
    private String latestVersion;

    /**
     * 版本总数
     */
    private Integer versionCount;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    /**
     * 插件版本信息列表
     */
    private List<PluginVersion> pluginVersionList;

}
