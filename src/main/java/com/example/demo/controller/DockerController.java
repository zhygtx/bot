package com.example.demo.controller;

import com.example.demo.pojo.entity.Docker;
import com.example.demo.pojo.entity.Result;
import com.example.demo.service.DockerService;
import com.example.demo.service.UserService;
import com.example.demo.util.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/docker")
public class DockerController {

    private final DockerService dockerService;
    private final UserService userService;
    private final AuthUtil authUtil;

    public DockerController(DockerService dockerService, UserService userService, AuthUtil authUtil) {
        this.dockerService = dockerService;
        this.userService = userService;
        this.authUtil = authUtil;
    }

    /**
     * 创建容器
     * @param request HTTP请求
     * @param napcatToken 用户设置的令牌
     */
    @PostMapping
    public Result<?> createContainer(HttpServletRequest request, String napcatToken, Long botQQ) {
        String userId = authUtil.getCurrentUserId(request);
        Result<?> result = dockerService.createContainer(userId,napcatToken,botQQ);
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
    public Result<?> getContainerInfo(HttpServletRequest request) {
        String userId = authUtil.getCurrentUserId(request);
        Docker docker = dockerService.getByUserId(userId);
        return Result.success(docker);
    }

    /**
     * 删除容器
     * @param request HTTP请求
     */
    @DeleteMapping
    public Result<String> deleteContainer(Long botQQ) {
        dockerService.deleteContainer(botQQ);
        return Result.success();
    }
}
