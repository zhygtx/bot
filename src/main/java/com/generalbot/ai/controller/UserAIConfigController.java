package com.generalbot.ai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.generalbot.ai.client.DynamicChatClientFactory;
import com.generalbot.ai.entity.UserAIConfig;
import com.generalbot.ai.service.UserAIConfigService;
import com.generalbot.common.api.Result;
import com.generalbot.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/user-ai-config")
public class UserAIConfigController {

    private final DynamicChatClientFactory dynamicChatClientFactory;
    private final UserAIConfigService userAIConfigService;

    public UserAIConfigController(UserAIConfigService userAIConfigService, DynamicChatClientFactory dynamicChatClientFactory) {
        this.userAIConfigService = userAIConfigService;
        this.dynamicChatClientFactory = dynamicChatClientFactory;
    }

    @PostMapping
    public Result<?> add(@AuthenticationPrincipal UserPrincipal user, @RequestBody UserAIConfig userAIConfig) {
        userAIConfig.setId(UUID.randomUUID().toString());
        userAIConfig.setUserId(user.userId());
        boolean save = userAIConfigService.save(userAIConfig);
        return save ? Result.success("添加成功",null) : Result.error(400,"添加失败");
    }

    @DeleteMapping
    public Result<?> delete(@AuthenticationPrincipal UserPrincipal user) {
        boolean remove = userAIConfigService.remove(new LambdaQueryWrapper<UserAIConfig>()
                .eq(UserAIConfig::getUserId, user.userId()));
        dynamicChatClientFactory.evictCache(user.userId());
        return remove ? Result.success("删除成功",null) : Result.error(400,"删除失败");
    }

    @PutMapping
    public Result<?> update(@AuthenticationPrincipal UserPrincipal user, @RequestBody UserAIConfig userAIConfig) {
        userAIConfig.setUserId(user.userId());
        boolean update = userAIConfigService.update(userAIConfig,
                Wrappers.lambdaUpdate(UserAIConfig.class)
                        .eq(UserAIConfig::getId, userAIConfig.getId()));
        dynamicChatClientFactory.evictCache(user.userId());
        return update ? Result.success("更新成功",null) : Result.error(400,"更新失败");
    }

    @GetMapping
    public Result<?> get(@AuthenticationPrincipal UserPrincipal user) {
        UserAIConfig userAIConfig = userAIConfigService.getOne(new LambdaQueryWrapper<UserAIConfig>()
                .eq(UserAIConfig::getUserId, user.userId()));
        return Result.success(null ,userAIConfig);
    }

    @GetMapping("/test")
    public Result<?> test(@AuthenticationPrincipal UserPrincipal user) throws RuntimeException {
        try {
            userAIConfigService.test(user.userId());
        } catch (RuntimeException e) {
            return Result.error(400,e.getMessage());
        }
        return Result.success("测试成功",null);
    }
}
