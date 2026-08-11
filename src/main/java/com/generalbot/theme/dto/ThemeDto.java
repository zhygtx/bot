package com.generalbot.theme.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 前端可直接应用的主题数据。
 * tokens 已经是最终变量表，前端不需要理解后端数据库结构。
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ThemeDto {

    /**
     * 主题 ID；内置主题是固定 key，自定义主题是数据库 ID。
     */
    private String id;

    /**
     * 主题名称。
     */
    private String name;

    /**
     * 主题模式，light / dark。
     */
    private String mode;

    /**
     * 是否为系统内置主题。
     */
    private boolean builtin;

    /**
     * CSS 变量令牌表。
     */
    private Map<String, String> tokens;
}
