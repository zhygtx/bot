package com.generalbot.theme.controller;

import com.generalbot.common.api.Result;
import com.generalbot.security.UserPrincipal;
import com.generalbot.theme.dto.ThemeDto;
import com.generalbot.theme.dto.ThemeListDto;
import com.generalbot.theme.dto.ThemeSaveRequest;
import com.generalbot.theme.dto.ThemeSwitchRequest;
import com.generalbot.theme.service.ThemeService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户主题接口。
 * 内置主题和用户自定义主题统一通过这里暴露给前端 Theme Studio。
 */
@RestController
@RequestMapping("/theme")
public class ThemeController {

    private final ThemeService themeService;

    public ThemeController(ThemeService themeService) {
        this.themeService = themeService;
    }

    /**
     * 获取当前用户正在使用的主题。
     */
    @GetMapping("/current")
    public Result<ThemeDto> current(@AuthenticationPrincipal UserPrincipal user, @RequestParam(defaultValue = "light") String mode) {
        return Result.success(null, themeService.current(user.userId(), mode));
    }

    /**
     * 查询内置主题和当前用户自定义主题。
     */
    @GetMapping("/list")
    public Result<ThemeListDto> list(@AuthenticationPrincipal UserPrincipal user) {
        return Result.success(null, themeService.list(user.userId()));
    }

    /**
     * 新建当前用户的自定义主题。
     */
    @PostMapping
    public Result<ThemeDto> create(@AuthenticationPrincipal UserPrincipal user, @RequestBody ThemeSaveRequest request) {
        return Result.success("主题已创建", themeService.create(user.userId(), request));
    }

    /**
     * 更新当前用户的自定义主题。
     */
    @PutMapping("/{id}")
    public Result<ThemeDto> update(@AuthenticationPrincipal UserPrincipal user, @PathVariable String id, @RequestBody ThemeSaveRequest request) {
        return Result.success("主题已保存", themeService.update(user.userId(), id, request));
    }

    /**
     * 删除当前用户的自定义主题。
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@AuthenticationPrincipal UserPrincipal user, @PathVariable String id) {
        themeService.delete(user.userId(), id);
        return Result.success("主题已删除", null);
    }

    /**
     * 切换当前用户正在使用的主题。
     */
    @PutMapping("/current")
    public Result<ThemeDto> switchCurrent(@AuthenticationPrincipal UserPrincipal user, @RequestBody ThemeSwitchRequest request) {
        return Result.success("主题已切换", themeService.switchCurrent(user.userId(), request));
    }

    /**
     * 紧急重置当前用户的亮色/暗色主题到内置默认主题。
     */
    @PostMapping("/reset")
    public Result<String> reset(@AuthenticationPrincipal UserPrincipal user) {
        themeService.reset(user.userId());
        return Result.success("主题已重置", null);
    }
}
