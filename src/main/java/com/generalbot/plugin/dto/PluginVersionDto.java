package com.generalbot.plugin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 插件版本信息DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PluginVersionDto {

    /**
     * 插件版本id
     */
    private String id;

    /**
     * 插件版本号
     */
    private String version;
}