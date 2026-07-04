package com.example.demo.ai.ai.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompileCodeDto {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 会话ID
     */
    private String conversationId;

    /**
     * 插件名称
     */
    private String pluginName;

    /**
     * 插件描述
     */
    private String pluginDescription;

    /**
     * 插件ID
     */
    private String pluginId;

    /**
     * 是否公开
     */
    private Boolean isPublic;

    /**
     * 插件实体类包名
     */
    private String entityPackage = "entity";

    /**
     * 插件方法类包名
     */
    private String methodPackage = "service";

    /**
     * 插件版本
     */
    private String pluginVersion;

    /**
     * 插件变更描述
     */
    private String pluginChangeDescription;

}
