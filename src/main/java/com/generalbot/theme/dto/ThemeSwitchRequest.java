package com.generalbot.theme.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 切换当前主题的请求。
 * type 用来区分内置主题和自定义主题，themeKey 保存内置 key 或用户主题 ID。
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ThemeSwitchRequest {

    /**
     * 主题类型：BUILTIN / CUSTOM。
     */
    private String type;

    /**
     * 主题键。
     */
    private String themeKey;
}
