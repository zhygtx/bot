package com.generalbot.theme.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 主题列表页数据。
 * activeThemeIds 按 light/dark 返回，前端可以分别标记两个模式当前启用的主题。
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ThemeListDto {

    /**
     * 每个模式当前启用主题 ID，key 为 light/dark。
     */
    private Map<String, String> activeThemeIds;

    /**
     * 内置主题和用户自定义主题的合并列表。
     */
    private List<ThemeDto> themes;
}
