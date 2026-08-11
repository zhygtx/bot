package com.generalbot.theme.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 创建或更新用户主题的请求。
 * tokens 使用 Map 接收，服务层负责校验令牌名和令牌值，避免任意 CSS 注入。
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ThemeSaveRequest {

    /**
     * 主题名称。
     */
    private String name;

    /**
     * 主题模式，light / dark。
     */
    private String mode;

    /**
     * 用户配置的 CSS 变量令牌。
     */
    private Map<String, String> tokens;

    /**
     * 用户自定义 CSS；空字符串表示清空高级样式覆盖。
     */
    private String customCss;
}
