package com.generalbot.docker.controller;

import com.generalbot.docker.entity.Docker;
import com.generalbot.common.api.Result;
import com.generalbot.security.UserPrincipal;
import com.generalbot.docker.service.DockerService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/docker")
public class DockerController {

    private final DockerService dockerService;

    public DockerController(DockerService dockerService) {
        this.dockerService = dockerService;
    }

    /**
     * 创建容器
     * @param request HTTP请求
     * @param napcatToken 用户设置的令牌
     */
    @PostMapping
    public Result<?> createContainer(@AuthenticationPrincipal UserPrincipal user, String napcatToken, Long botQQ) {
        Result<?> result = dockerService.createContainer(user.userId(), napcatToken, botQQ);
        if (!result.getCode().equals(0)){
            return result;
        }
        return Result.success("创建容器成功,请根据端口访问napcatUI进行扫码登录", result.getData());
    }

    /**
     * 获取容器信息
     * @param request HTTP请求
     */
    @GetMapping
    public Result<?> getContainerInfo(@AuthenticationPrincipal UserPrincipal user) {
        Docker docker = dockerService.getByUserId(user.userId());
        return Result.success(null, docker);
    }

    /**
     * 删除容器
     * @param botQQ botQQ号
     */
    @DeleteMapping
    public Result<String> deleteContainer(Long botQQ) {
        dockerService.deleteContainer(botQQ);
        return Result.success();
    }
}
