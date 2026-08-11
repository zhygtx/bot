package com.generalbot.theme.service;

import com.generalbot.theme.dto.ThemeDto;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 内置主题注册表。
 * 默认主题固定写在代码里，避免每个用户复制一份默认主题数据到数据库。
 */
@Component
public class BuiltinThemeRegistry {

    /**
     * 默认亮色主题 ID。
     */
    public static final String DEFAULT_LIGHT_ID = "default-light";

    /**
     * 默认暗色主题 ID。
     */
    public static final String DEFAULT_DARK_ID = "default-dark";

    private final Map<String, ThemeDto> builtinThemes;

    public BuiltinThemeRegistry() {
        builtinThemes = new LinkedHashMap<>();
        builtinThemes.put(DEFAULT_LIGHT_ID, new ThemeDto(DEFAULT_LIGHT_ID, "默认亮色", "light", true, lightTokens(), ""));
        builtinThemes.put(DEFAULT_DARK_ID, new ThemeDto(DEFAULT_DARK_ID, "默认暗色", "dark", true, darkTokens(), ""));
    }

    /**
     * 查询全部内置主题。
     * 返回复制对象，避免调用方修改注册表里的全局主题。
     */
    public List<ThemeDto> list() {
        return builtinThemes.values().stream().map(this::copyTheme).toList();
    }

    /**
     * 按 key 查询内置主题。
     * 未找到时直接报错，因为引用不存在的内置主题属于数据错误。
     */
    public ThemeDto get(String key) {
        ThemeDto theme = builtinThemes.get(key);
        if (theme == null) {
            throw new IllegalArgumentException("内置主题不存在：" + key);
        }
        return copyTheme(theme);
    }

    /**
     * 判断主题 key 是否为内置主题。
     */
    public boolean exists(String key) {
        return builtinThemes.containsKey(key);
    }

    /**
     * 获取默认亮色主题。
     */
    public ThemeDto defaultTheme() {
        return get(DEFAULT_LIGHT_ID);
    }

    /**
     * 返回所有内置主题声明过的 token 名称。
     * 服务层用它作为主题白名单，避免只取默认亮色主题导致亮暗 token 漂移时误拒绝。
     */
    public Set<String> allowedTokenNames() {
        return builtinThemes.values().stream()
                .flatMap(theme -> theme.getTokens().keySet().stream())
                .collect(Collectors.toUnmodifiableSet());
    }

    private ThemeDto copyTheme(ThemeDto theme) {
        return new ThemeDto(theme.getId(), theme.getName(), theme.getMode(), theme.isBuiltin(), new LinkedHashMap<>(theme.getTokens()), theme.getCustomCss());
    }

    private Map<String, String> lightTokens() {
        Map<String, String> tokens = new LinkedHashMap<>();
        tokens.put("--app-bg", "#f5f7fa");
        tokens.put("--app-bg-soft", "#ffffff");
        tokens.put("--app-bg-muted", "#f9fafc");
        tokens.put("--app-header-bg", "rgba(255, 255, 255, 0.96)");
        tokens.put("--app-card-bg", "#ffffff");
        tokens.put("--app-card-bg-hover", "#ffffff");
        tokens.put("--app-panel-bg", "#ffffff");
        tokens.put("--app-panel-muted", "#f5f7fa");
        tokens.put("--app-surface", "#ffffff");
        tokens.put("--sidebar-bg", "#ffffff");
        tokens.put("--sidebar-primary", "#1677ff");
        tokens.put("--sidebar-text", "#4e5969");
        tokens.put("--sidebar-text-primary", "#1d2129");
        tokens.put("--sidebar-text-active", "#1677ff");
        tokens.put("--sidebar-icon", "#86909c");
        tokens.put("--sidebar-icon-active", "#1677ff");
        tokens.put("--sidebar-hover-bg", "#f0f5ff");
        tokens.put("--sidebar-active-bg", "#e6f0ff");
        tokens.put("--sidebar-border", "#e4e7ed");
        tokens.put("--sidebar-divider", "#ebedf0");
        tokens.put("--page-bg", "#f5f7fa");
        tokens.put("--app-text", "#303133");
        tokens.put("--app-text-soft", "#606266");
        tokens.put("--app-text-muted", "#909399");
        tokens.put("--app-text-disabled", "#c0c4cc");
        tokens.put("--app-border", "#e6e6e6");
        tokens.put("--app-border-soft", "#eef2f7");
        tokens.put("--app-primary", "#1677ff");
        tokens.put("--app-primary-strong", "#1677ff");
        tokens.put("--app-primary-hover", "#4096ff");
        tokens.put("--app-primary-soft", "rgba(22, 119, 255, 0.1)");
        tokens.put("--app-primary-soft-strong", "rgba(22, 119, 255, 0.14)");
        tokens.put("--app-primary-soft-weak", "rgba(22, 119, 255, 0.08)");
        tokens.put("--app-primary-border", "rgba(22, 119, 255, 0.28)");
        tokens.put("--app-primary-border-strong", "rgba(22, 119, 255, 0.3)");
        tokens.put("--app-success", "#67c23a");
        tokens.put("--app-success-soft", "rgba(103, 194, 58, 0.1)");
        tokens.put("--app-success-border", "rgba(103, 194, 58, 0.3)");
        tokens.put("--app-danger", "#f56c6c");
        tokens.put("--app-danger-soft", "rgba(245, 108, 108, 0.08)");
        tokens.put("--app-danger-soft-strong", "rgba(245, 108, 108, 0.15)");
        tokens.put("--app-danger-border", "rgba(245, 108, 108, 0.3)");
        tokens.put("--app-warning", "#e6a23c");
        tokens.put("--app-on-primary", "#fff");
        tokens.put("--app-code-bg", "#1f2933");
        tokens.put("--app-code-text", "#eef8fb");
        tokens.put("--app-canvas-bg", "#ffffff");
        tokens.put("--app-canvas-dot", "#c8c8c8");
        tokens.put("--app-header-text", "#ffffff");
        tokens.put("--app-mobile-header-bg", "#1677ff");
        tokens.put("--chart-primary", "#409eff");
        tokens.put("--chart-warning", "#e6a23c");
        tokens.put("--auth-bg", "linear-gradient(135deg, #f5f7fa 0%, #e9eef6 100%)");
        tokens.put("--app-surface-raised", "#ffffff");
        tokens.put("--app-surface-raised-soft", "#ffffff");
        tokens.put("--app-surface-border", "#f0f0f0");
        tokens.put("--app-surface-shadow", "0 2px 12px rgba(0, 0, 0, 0.1)");
        tokens.put("--app-shadow", "0 4px 12px rgba(0, 0, 0, 0.1)");
        tokens.put("--app-shadow-hover", "0 8px 22px rgba(15, 23, 42, 0.14)");
        tokens.put("--app-header-shadow", "0 2px 8px rgba(0, 0, 0, 0.06)");
        tokens.put("--app-radius-card", "8px");
        tokens.put("--app-radius-control", "6px");
        tokens.put("--app-radius-pill", "999px");
        tokens.put("--app-radius-floating", "10px");
        tokens.put("--app-blur-surface", "10px");
        tokens.put("--app-backdrop-filter", "blur(10px)");
        tokens.put("--app-header-backdrop-filter", "blur(10px)");
        tokens.put("--app-floating-backdrop-filter", "blur(10px)");
        tokens.put("--app-border-card", "1px solid var(--app-border-soft)");
        tokens.put("--app-border-panel", "1px solid var(--app-border-soft)");
        tokens.put("--app-border-control", "1px solid var(--app-border)");
        tokens.put("--app-border-focus", "0 0 0 2px var(--app-primary-border)");
        tokens.put("--app-gap", "16px");
        tokens.put("--app-card-padding", "20px");
        tokens.put("--app-panel-padding", "16px");
        tokens.put("--app-control-height", "32px");
        tokens.put("--app-toolbar-height", "56px");
        tokens.put("--app-list-item-height", "40px");
        tokens.put("--app-transition-fast", "0.15s ease");
        tokens.put("--app-transition", "0.3s ease");
        tokens.put("--app-transition-slow", "0.45s ease");
        tokens.put("--app-ease-standard", "cubic-bezier(0.2, 0.8, 0.2, 1)");
        tokens.put("--app-hover-translate", "translateY(-2px)");
        tokens.put("--app-hover-scale", "scale(1.02)");
        tokens.put("--app-canvas-grid-size", "20px");
        tokens.put("--workflow-plugin-panel-bg", "var(--app-surface-raised)");
        tokens.put("--workflow-toolbar-bg", "var(--app-surface-raised-soft)");
        tokens.put("--workflow-config-panel-bg", "var(--app-surface-raised)");
        tokens.put("--workflow-node-bg", "var(--app-surface-raised)");
        tokens.put("--workflow-node-param-bg", "var(--app-bg)");
        tokens.put("--workflow-node-port-bg", "var(--app-surface-raised)");
        tokens.put("--workflow-node-border", "1px solid var(--app-border)");
        tokens.put("--workflow-node-selected-outline", "0 0 0 2px var(--app-primary-border-strong)");
        tokens.put("--workflow-node-shadow", "var(--app-shadow)");
        tokens.put("--workflow-panel-shadow", "var(--app-shadow)");
        tokens.put("--workflow-node-radius", "8px");
        tokens.put("--workflow-port-size", "12px");
        return tokens;
    }

    private Map<String, String> darkTokens() {
        Map<String, String> tokens = new LinkedHashMap<>();
        tokens.put("--app-bg", "#0f141a");
        tokens.put("--app-bg-soft", "#0d1724");
        tokens.put("--app-bg-muted", "#111d2c");
        tokens.put("--app-header-bg", "rgba(9, 17, 29, 0.92)");
        tokens.put("--app-card-bg", "rgba(13, 23, 36, 0.96)");
        tokens.put("--app-card-bg-hover", "rgba(20, 32, 48, 0.98)");
        tokens.put("--app-panel-bg", "rgba(13, 23, 36, 0.95)");
        tokens.put("--app-panel-muted", "rgba(20, 32, 48, 0.88)");
        tokens.put("--app-surface", "#0d1724");
        tokens.put("--sidebar-bg", "#141b26");
        tokens.put("--sidebar-primary", "#4096ff");
        tokens.put("--sidebar-text", "#a4b0be");
        tokens.put("--sidebar-text-primary", "#e6edf3");
        tokens.put("--sidebar-text-active", "#4096ff");
        tokens.put("--sidebar-icon", "#8896a5");
        tokens.put("--sidebar-icon-active", "#4096ff");
        tokens.put("--sidebar-hover-bg", "#1a2432");
        tokens.put("--sidebar-active-bg", "#1e2a38");
        tokens.put("--sidebar-border", "#263140");
        tokens.put("--sidebar-divider", "#1f2937");
        tokens.put("--page-bg", "#0f141a");
        tokens.put("--app-text", "#e5eef9");
        tokens.put("--app-text-soft", "#a9b9cc");
        tokens.put("--app-text-muted", "#76879c");
        tokens.put("--app-text-disabled", "#64748b");
        tokens.put("--app-border", "rgba(100, 139, 184, 0.24)");
        tokens.put("--app-border-soft", "rgba(100, 139, 184, 0.16)");
        tokens.put("--app-primary", "#4096ff");
        tokens.put("--app-primary-strong", "#4096ff");
        tokens.put("--app-primary-hover", "#4096ff");
        tokens.put("--app-primary-soft", "rgba(64, 150, 255, 0.12)");
        tokens.put("--app-primary-soft-strong", "rgba(64, 150, 255, 0.18)");
        tokens.put("--app-primary-soft-weak", "rgba(64, 150, 255, 0.08)");
        tokens.put("--app-primary-border", "rgba(64, 150, 255, 0.4)");
        tokens.put("--app-primary-border-strong", "rgba(64, 150, 255, 0.55)");
        tokens.put("--app-success", "#67c23a");
        tokens.put("--app-success-soft", "rgba(103, 194, 58, 0.12)");
        tokens.put("--app-success-border", "rgba(103, 194, 58, 0.3)");
        tokens.put("--app-danger", "#f56c6c");
        tokens.put("--app-danger-soft", "rgba(245, 108, 108, 0.12)");
        tokens.put("--app-danger-soft-strong", "rgba(245, 108, 108, 0.2)");
        tokens.put("--app-danger-border", "rgba(245, 108, 108, 0.4)");
        tokens.put("--app-warning", "#e6a23c");
        tokens.put("--app-on-primary", "#fff");
        tokens.put("--app-code-bg", "#1f2933");
        tokens.put("--app-code-text", "#eef8fb");
        tokens.put("--app-canvas-bg", "#08111d");
        tokens.put("--app-canvas-dot", "#3a5068");
        tokens.put("--app-header-text", "#ffffff");
        tokens.put("--app-mobile-header-bg", "#2563eb");
        tokens.put("--chart-primary", "#4096ff");
        tokens.put("--chart-warning", "#e6a23c");
        tokens.put("--auth-bg", "linear-gradient(135deg, #0f141a 0%, #15181d 100%)");
        tokens.put("--app-surface-raised", "rgba(13, 23, 36, 0.96)");
        tokens.put("--app-surface-raised-soft", "rgba(13, 23, 36, 0.95)");
        tokens.put("--app-surface-border", "rgba(100, 139, 184, 0.24)");
        tokens.put("--app-surface-shadow", "0 16px 40px rgba(0, 0, 0, 0.28)");
        tokens.put("--app-shadow", "0 16px 40px rgba(0, 0, 0, 0.28)");
        tokens.put("--app-shadow-hover", "0 18px 44px rgba(0, 0, 0, 0.38)");
        tokens.put("--app-header-shadow", "0 12px 30px rgba(0, 0, 0, 0.24)");
        tokens.put("--app-radius-card", "8px");
        tokens.put("--app-radius-control", "6px");
        tokens.put("--app-radius-pill", "999px");
        tokens.put("--app-radius-floating", "10px");
        tokens.put("--app-blur-surface", "10px");
        tokens.put("--app-backdrop-filter", "blur(10px)");
        tokens.put("--app-header-backdrop-filter", "blur(10px)");
        tokens.put("--app-floating-backdrop-filter", "blur(10px)");
        tokens.put("--app-border-card", "1px solid var(--app-border-soft)");
        tokens.put("--app-border-panel", "1px solid var(--app-border-soft)");
        tokens.put("--app-border-control", "1px solid var(--app-border)");
        tokens.put("--app-border-focus", "0 0 0 2px var(--app-primary-border)");
        tokens.put("--app-gap", "16px");
        tokens.put("--app-card-padding", "20px");
        tokens.put("--app-panel-padding", "16px");
        tokens.put("--app-control-height", "32px");
        tokens.put("--app-toolbar-height", "56px");
        tokens.put("--app-list-item-height", "40px");
        tokens.put("--app-transition-fast", "0.15s ease");
        tokens.put("--app-transition", "0.3s ease");
        tokens.put("--app-transition-slow", "0.45s ease");
        tokens.put("--app-ease-standard", "cubic-bezier(0.2, 0.8, 0.2, 1)");
        tokens.put("--app-hover-translate", "translateY(-2px)");
        tokens.put("--app-hover-scale", "scale(1.02)");
        tokens.put("--app-canvas-grid-size", "20px");
        tokens.put("--workflow-plugin-panel-bg", "var(--app-surface-raised)");
        tokens.put("--workflow-toolbar-bg", "var(--app-surface-raised-soft)");
        tokens.put("--workflow-config-panel-bg", "var(--app-surface-raised)");
        tokens.put("--workflow-node-bg", "var(--app-surface-raised)");
        tokens.put("--workflow-node-param-bg", "var(--app-bg)");
        tokens.put("--workflow-node-port-bg", "var(--app-surface-raised)");
        tokens.put("--workflow-node-border", "1px solid var(--app-border)");
        tokens.put("--workflow-node-selected-outline", "0 0 0 2px var(--app-primary-border-strong)");
        tokens.put("--workflow-node-shadow", "var(--app-shadow)");
        tokens.put("--workflow-panel-shadow", "var(--app-shadow)");
        tokens.put("--workflow-node-radius", "8px");
        tokens.put("--workflow-port-size", "12px");
        return tokens;
    }
}
