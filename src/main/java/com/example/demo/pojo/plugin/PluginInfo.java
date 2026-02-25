package com.example.demo.pojo.plugin;

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
     * 插件版本
     */
    private String version;

    /**
     * 插件兼容的版本
     */
    private String compatibleVersion;

    /**
     * 插件作者
     */
    private String authorId;

    /**
     * 插件存储路径
     */
    private String path;

    /**
     * 插件实体类包名
     */
    private String entityPackage;

    /**
     * 插件方法类包名
     */
    private String methodPackage;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否公开
     */
    private Boolean isPublic;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 文件MD5校验码
     */
    private String fileMd5;

    /**
     * 插件实体类信息
     */
    private List<EntityInfo> entityInfoList;

    /**
     * 插件方法类信息列表
     */
    private List<MethodClassInfo> methodClassInfoList;

}
