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
public class PluginVersion {

    /**
     * 版本ID
     */
    private String id;

    /**
     * 插件ID
     */
    private String pluginId;

    /**
     * 版本号
     */
    private String version;

    /**
     * 插件文件路径
     */
    private String path;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 文件MD5
     */
    private String fileMd5;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 版本变更说明
     */
    private String changelog;

    /**
     * 插件实体类包名
     */
    private String entityPackage;

    /**
     * 插件方法类包名
     */
    private String methodPackage;

    /**
     * 此版本所兼容的版本（JSON）
     */
    private String compatibleVersion;

    /**
     * 插件实体类信息
     */
    private List<EntityInfo> entityInfoList;

    /**
     * 插件方法类信息列表
     */
    private List<MethodClassInfo> methodClassInfoList;
}
