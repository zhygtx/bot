package com.generalbot.theme.service;

import com.generalbot.theme.dto.ThemeDto;
import com.generalbot.theme.dto.ThemeListDto;
import com.generalbot.theme.dto.ThemeSaveRequest;
import com.generalbot.theme.dto.ThemeSwitchRequest;

/**
 * 用户主题服务。
 */
public interface ThemeService {

    /**
     * 获取当前用户指定模式正在使用的主题。
     */
    ThemeDto current(String userId, String mode);

    /**
     * 获取当前用户可用主题列表。
     */
    ThemeListDto list(String userId);

    /**
     * 创建用户自定义主题。
     */
    ThemeDto create(String userId, ThemeSaveRequest request);

    /**
     * 更新用户自定义主题。
     */
    ThemeDto update(String userId, String id, ThemeSaveRequest request);

    /**
     * 删除用户自定义主题。
     */
    void delete(String userId, String id);

    /**
     * 设置当前用户正在使用的主题。
     */
    ThemeDto switchCurrent(String userId, ThemeSwitchRequest request);
}
