package com.example.demo.ai.ai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.ai.ai.client.DynamicChatClientFactory;
import com.example.demo.ai.ai.pojo.entity.UserAIConfig;
import com.example.demo.ai.ai.service.UserAIConfigService;
import com.example.demo.pojo.entity.Result;
import com.example.demo.security.UserPrincipal;
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
    public Result<?> update(@RequestBody UserAIConfig userAIConfig) {
        boolean update = userAIConfigService.update(userAIConfig,
                Wrappers.lambdaUpdate(UserAIConfig.class)
                        .eq(UserAIConfig::getId, userAIConfig.getId()));
        dynamicChatClientFactory.evictCache(userAIConfig.getUserId());
        return update ? Result.success("更新成功",null) : Result.error(400,"更新失败");
    }

    @GetMapping
    public Result<?> get(@AuthenticationPrincipal UserPrincipal user) {
        UserAIConfig userAIConfig = userAIConfigService.getOne(new LambdaQueryWrapper<UserAIConfig>()
                .eq(UserAIConfig::getUserId, user.userId()));
        return userAIConfig != null ? Result.success(null ,userAIConfig) : Result.error(400,"查询失败");
    }
}
