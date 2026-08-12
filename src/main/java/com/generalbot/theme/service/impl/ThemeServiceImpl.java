package com.generalbot.theme.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.generalbot.theme.dto.ThemeDto;
import com.generalbot.theme.dto.ThemeListDto;
import com.generalbot.theme.dto.ThemeSaveRequest;
import com.generalbot.theme.dto.ThemeSwitchRequest;
import com.generalbot.theme.entity.UserTheme;
import com.generalbot.theme.entity.UserThemeSetting;
import com.generalbot.theme.mapper.UserThemeMapper;
import com.generalbot.theme.mapper.UserThemeSettingMapper;
import com.generalbot.theme.service.BuiltinThemeRegistry;
import com.generalbot.theme.service.ThemeService;
import com.generalbot.theme.util.ThemeCssValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 用户主题服务实现。
 * 内置主题由代码注册，自定义主题由数据库保存，服务层统一合并成前端可直接应用的 ThemeDto。
 */
@Service
public class ThemeServiceImpl implements ThemeService {

    private static final String THEME_TYPE_BUILTIN = "BUILTIN";
    private static final String THEME_TYPE_CUSTOM = "CUSTOM";
    private static final Set<String> SUPPORTED_MODES = Set.of("light", "dark");
    private static final Pattern TOKEN_VALUE_PATTERN = Pattern.compile("^[#a-zA-Z0-9(),.%\\s\\-/]+$");
    private static final Pattern UNSAFE_TOKEN_VALUE_PATTERN = Pattern.compile("(?i)(url\\s*\\(|expression\\s*\\(|@import|javascript:)");

    private final UserThemeMapper userThemeMapper;
    private final UserThemeSettingMapper userThemeSettingMapper;
    private final BuiltinThemeRegistry builtinThemeRegistry;
    private final ObjectMapper objectMapper;
    private final Set<String> allowedTokens;

    public ThemeServiceImpl(UserThemeMapper userThemeMapper, UserThemeSettingMapper userThemeSettingMapper, BuiltinThemeRegistry builtinThemeRegistry, ObjectMapper objectMapper) {
        this.userThemeMapper = userThemeMapper;
        this.userThemeSettingMapper = userThemeSettingMapper;
        this.builtinThemeRegistry = builtinThemeRegistry;
        this.objectMapper = objectMapper;
        this.allowedTokens = builtinThemeRegistry.allowedTokenNames();
    }

    /**
     * 获取指定模式的当前主题。
     * 用户从未选择主题时直接返回同模式默认主题，不写库，避免无意义的设置记录。
     */
    @Override
    public ThemeDto current(String userId, String mode) {
        String normalizedMode = normalizeMode(mode);
        UserThemeSetting setting = userThemeSettingMapper.selectByUserIdAndMode(userId, normalizedMode);
        if (setting == null) {
            return defaultThemeForMode(normalizedMode);
        }
        return resolveSetting(userId, setting, normalizedMode);
    }

    /**
     * 获取主题列表。
     * 内置主题始终排在前面，自定义主题按更新时间倒序由 mapper 返回。
     */
    @Override
    public ThemeListDto list(String userId) {
        List<ThemeDto> themes = new ArrayList<>(builtinThemeRegistry.list());
        userThemeMapper.selectByUserId(userId).stream()
                .map(this::toDto)
                .forEach(themes::add);
        Map<String, String> activeThemeIds = new LinkedHashMap<>();
        activeThemeIds.put("light", current(userId, "light").getId());
        activeThemeIds.put("dark", current(userId, "dark").getId());
        return new ThemeListDto(activeThemeIds, themes);
    }

    /**
     * 创建用户主题。
     * 新主题只保存用户提交的最终 token 快照，后续修改默认主题不会悄悄改变用户自定义主题。
     */
    @Override
    @Transactional
    public ThemeDto create(String userId, ThemeSaveRequest request) {
        validateSaveRequest(request);
        LocalDateTime now = LocalDateTime.now();
        UserTheme theme = new UserTheme();
        theme.setId(UUID.randomUUID().toString());
        theme.setUserId(userId);
        theme.setName(request.getName().trim());
        theme.setMode(request.getMode());
        theme.setTokens(writeTokens(request.getTokens()));
        theme.setCustomCss(ThemeCssValidator.validate(request.getCustomCss()));
        theme.setCreateTime(now);
        theme.setUpdateTime(now);
        userThemeMapper.insert(theme);
        return toDto(theme);
    }

    /**
     * 更新用户主题。
     * 只能更新当前用户自己的主题；查不到时直接报错，避免静默创建或跨用户修改。
     */
    @Override
    @Transactional
    public ThemeDto update(String userId, String id, ThemeSaveRequest request) {
        validateSaveRequest(request);
        UserTheme existing = requireUserTheme(userId, id);
        String previousMode = existing.getMode();
        UserThemeSetting previousModeSetting = userThemeSettingMapper.selectByUserIdAndMode(userId, previousMode);
        boolean wasActive = previousModeSetting != null
                && THEME_TYPE_CUSTOM.equals(previousModeSetting.getActiveThemeType())
                && id.equals(previousModeSetting.getActiveThemeKey());
        existing.setName(request.getName().trim());
        existing.setMode(request.getMode());
        existing.setTokens(writeTokens(request.getTokens()));
        existing.setCustomCss(ThemeCssValidator.validate(request.getCustomCss()));
        existing.setUpdateTime(LocalDateTime.now());
        userThemeMapper.update(existing);
        if (wasActive && !previousMode.equals(request.getMode())) {
            saveSetting(userId, previousMode, THEME_TYPE_BUILTIN, defaultThemeIdForMode(previousMode));
            saveSetting(userId, request.getMode(), THEME_TYPE_CUSTOM, id);
        }
        return toDto(existing);
    }

    /**
     * 删除用户主题。
     * 如果删除的是某个模式当前使用的主题，同事务内只把这个模式切回默认主题。
     */
    @Override
    @Transactional
    public void delete(String userId, String id) {
        UserTheme theme = requireUserTheme(userId, id);
        userThemeMapper.deleteByIdAndUserId(id, userId);
        UserThemeSetting setting = userThemeSettingMapper.selectByUserIdAndMode(userId, theme.getMode());
        if (setting != null && THEME_TYPE_CUSTOM.equals(setting.getActiveThemeType()) && id.equals(setting.getActiveThemeKey())) {
            saveSetting(userId, theme.getMode(), THEME_TYPE_BUILTIN, defaultThemeIdForMode(theme.getMode()));
        }
    }

    /**
     * 切换当前主题。
     * type 和 themeKey 都校验存在性，保证 setting 表不会指向不存在的主题。
     */
    @Override
    @Transactional
    public ThemeDto switchCurrent(String userId, ThemeSwitchRequest request) {
        if (request == null || !StringUtils.hasText(request.getType()) || !StringUtils.hasText(request.getThemeKey())) {
            throw new IllegalArgumentException("主题类型和主题键不能为空");
        }
        String type = request.getType().trim().toUpperCase();
        String themeKey = request.getThemeKey().trim();
        if (THEME_TYPE_BUILTIN.equals(type)) {
            if (!builtinThemeRegistry.exists(themeKey)) {
                throw new IllegalArgumentException("内置主题不存在：" + themeKey);
            }
            ThemeDto theme = builtinThemeRegistry.get(themeKey);
            saveSetting(userId, theme.getMode(), type, themeKey);
            return theme;
        }
        if (THEME_TYPE_CUSTOM.equals(type)) {
            UserTheme theme = requireUserTheme(userId, themeKey);
            saveSetting(userId, theme.getMode(), type, themeKey);
            return toDto(theme);
        }
        throw new IllegalArgumentException("不支持的主题类型：" + request.getType());
    }

    /**
     * 紧急重置：亮色和暗色都切回内置默认主题。
     * 不删除用户自定义主题，只修改当前激活槽位，保证页面能立即恢复正常。
     */
    @Override
    @Transactional
    public void reset(String userId) {
        saveSetting(userId, "light", THEME_TYPE_BUILTIN, defaultThemeIdForMode("light"));
        saveSetting(userId, "dark", THEME_TYPE_BUILTIN, defaultThemeIdForMode("dark"));
    }

    private ThemeDto resolveSetting(String userId, UserThemeSetting setting, String mode) {
        if (THEME_TYPE_BUILTIN.equals(setting.getActiveThemeType())) {
            if (!builtinThemeRegistry.exists(setting.getActiveThemeKey())) {
                saveSetting(userId, mode, THEME_TYPE_BUILTIN, defaultThemeIdForMode(mode));
                return defaultThemeForMode(mode);
            }
            ThemeDto theme = builtinThemeRegistry.get(setting.getActiveThemeKey());
            if (!mode.equals(theme.getMode())) {
                saveSetting(userId, mode, THEME_TYPE_BUILTIN, defaultThemeIdForMode(mode));
                return defaultThemeForMode(mode);
            }
            return theme;
        }
        if (THEME_TYPE_CUSTOM.equals(setting.getActiveThemeType())) {
            UserTheme theme = userThemeMapper.selectByIdAndUserId(setting.getActiveThemeKey(), userId);
            if (theme == null) {
                saveSetting(userId, mode, THEME_TYPE_BUILTIN, defaultThemeIdForMode(mode));
                return defaultThemeForMode(mode);
            }
            if (!mode.equals(theme.getMode())) {
                saveSetting(userId, mode, THEME_TYPE_BUILTIN, defaultThemeIdForMode(mode));
                return defaultThemeForMode(mode);
            }
            return toDto(theme);
        }
        throw new IllegalStateException("不支持的主题类型：" + setting.getActiveThemeType());
    }

    private void saveSetting(String userId, String mode, String type, String themeKey) {
        UserThemeSetting setting = new UserThemeSetting();
        setting.setUserId(userId);
        setting.setMode(mode);
        setting.setActiveThemeType(type);
        setting.setActiveThemeKey(themeKey);
        setting.setUpdateTime(LocalDateTime.now());
        userThemeSettingMapper.upsert(setting);
    }

    private UserTheme requireUserTheme(String userId, String id) {
        UserTheme theme = userThemeMapper.selectByIdAndUserId(id, userId);
        if (theme == null) {
            throw new IllegalArgumentException("用户主题不存在：" + id);
        }
        return theme;
    }

    private String normalizeMode(String mode) {
        String normalizedMode = StringUtils.hasText(mode) ? mode.trim().toLowerCase() : "light";
        if (!SUPPORTED_MODES.contains(normalizedMode)) {
            throw new IllegalArgumentException("主题模式只支持 light 或 dark");
        }
        return normalizedMode;
    }

    private ThemeDto defaultThemeForMode(String mode) {
        return builtinThemeRegistry.get(defaultThemeIdForMode(mode));
    }

    private String defaultThemeIdForMode(String mode) {
        return "dark".equals(mode) ? BuiltinThemeRegistry.DEFAULT_DARK_ID : BuiltinThemeRegistry.DEFAULT_LIGHT_ID;
    }

    private void validateSaveRequest(ThemeSaveRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("主题配置不能为空");
        }
        if (!StringUtils.hasText(request.getName())) {
            throw new IllegalArgumentException("主题名称不能为空");
        }
        request.setMode(normalizeMode(request.getMode()));
        if (!SUPPORTED_MODES.contains(request.getMode())) {
            throw new IllegalArgumentException("主题模式只支持 light 或 dark");
        }
        if (request.getTokens() == null || request.getTokens().isEmpty()) {
            throw new IllegalArgumentException("主题令牌不能为空");
        }
        request.getTokens().forEach(this::validateToken);
    }

    private void validateToken(String name, String value) {
        if (!allowedTokens.contains(name)) {
            throw new IllegalArgumentException("不支持的主题令牌：" + name);
        }
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("主题令牌值不能为空：" + name);
        }
        if (value.length() > 320) {
            throw new IllegalArgumentException("主题令牌值过长：" + name);
        }
        if (UNSAFE_TOKEN_VALUE_PATTERN.matcher(value).find()) {
            throw new IllegalArgumentException("主题令牌值包含不安全内容：" + name);
        }
        if (!TOKEN_VALUE_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("主题令牌值包含非法字符：" + name);
        }
    }

    private ThemeDto toDto(UserTheme theme) {
        return new ThemeDto(theme.getId(), theme.getName(), theme.getMode(), false, readTokens(theme.getTokens()), theme.getCustomCss() == null ? "" : theme.getCustomCss());
    }

    private Map<String, String> readTokens(String tokens) {
        try {
            return objectMapper.readValue(tokens, new TypeReference<LinkedHashMap<String, String>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("主题令牌 JSON 解析失败", e);
        }
    }

    private String writeTokens(Map<String, String> tokens) {
        try {
            return objectMapper.writeValueAsString(tokens);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("主题令牌 JSON 序列化失败", e);
        }
    }
}
