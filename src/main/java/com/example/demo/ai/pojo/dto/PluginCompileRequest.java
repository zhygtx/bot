package com.example.demo.ai.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI 插件编译上传请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PluginCompileRequest {

    /** AI 生成的源码文件列表 */
    private List<SourceFile> files;

    /** 插件名称 */
    private String name;

    /** 插件描述 */
    private String description;

    /** 版本号 */
    private String version;

    /** 版本变更说明 */
    private String changelog;

    /** 实体类包名 */
    private String entityPackage;

    /** 方法类包名 */
    private String methodPackage;

    /** 是否公开 */
    private Boolean isPublic;

    /** 关联的会话 ID（编译上传成功后更新对话状态） */
    private String conversationId;
}
