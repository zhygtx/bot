package com.generalbot.theme.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户当前启用的主题设置。
 * 自定义主题和当前选择分开保存，避免为了记录“当前使用默认主题”而复制内置主题数据。
 * 每个用户按 light/dark 各保存一个当前主题，明暗切换时直接读取对应槽位。
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserThemeSetting {

    /**
     * 用户 ID。
     */
    private String userId;

    /**
     * 主题模式：light / dark。
     */
    private String mode;

    /**
     * 当前主题类型：BUILTIN 表示内置主题，CUSTOM 表示用户自定义主题。
     */
    private String activeThemeType;

    /**
     * 当前主题键：内置主题使用固定 key，自定义主题使用 user_theme.id。
     */
    private String activeThemeKey;

    /**
     * 最后修改时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
