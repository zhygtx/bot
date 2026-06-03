package com.example.demo.controller;

import com.example.demo.pojo.entity.Docker;
import com.example.demo.pojo.entity.Result;
import com.example.demo.pojo.entity.User;
import com.example.demo.service.DockerService;
import com.example.demo.service.UserService;
import com.example.demo.util.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @RequestMapping("/create")
    public Result<?> createContainer(HttpServletRequest request, String napcatToken) {
        String userId = authUtil.getCurrentUserId(request);
        User user = userService.selectById(userId);
        if (user.getBotQQ() == null){
            return Result.error(400,"请先绑定BotQQ");
        }
        Result<?> result = dockerService.createContainer(user,napcatToken);
        if (!result.getCode().equals(0)){
            return result;
        }
        return Result.success("创建容器成功,请根据端口访问napcatUI进行扫码登录", result.getData());
    }

    /**
     * 获取容器信息
     * @param request HTTP请求
     */
    @RequestMapping("/info")
    public Result<?> getContainerInfo(HttpServletRequest request) {
        String userId = authUtil.getCurrentUserId(request);
        Docker docker = dockerService.getByUserId(userId);
        return Result.success(docker);
    }

    /**
     * 删除容器
     * @param request HTTP请求
     */
    @RequestMapping("/delete")
    public Result<String> deleteContainer(HttpServletRequest request) {
        String userId = authUtil.getCurrentUserId(request);
        User user = userService.selectById(userId);
        dockerService.deleteContainer(user);
        return Result.success();
    }
}
