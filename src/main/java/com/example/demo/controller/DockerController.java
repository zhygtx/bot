package com.example.demo.controller;

import com.example.demo.docker.DockerService;
import com.example.demo.pojo.Result;
import com.example.demo.pojo.User;
import com.example.demo.service.UserService;
import com.example.demo.utils.JWTUtil;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/docker")
public class DockerController {

    private final JWTUtil jwtUtil;
    private final DockerService dockerService;
    private final UserService userService;

    public DockerController(JWTUtil jwtUtil, DockerService dockerService, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.dockerService = dockerService;
        this.userService = userService;
    }

    /**
     * 创建容器
     * @param token 容器令牌
     * @param napcatToken 用户设置的令牌
     */
    @RequestMapping("/create")
    public Result<Integer> createContainer(@RequestHeader("Authorization") String token, @RequestParam String napcatToken) {
        String account = jwtUtil.getAccountFromToken(token);
        User user = userService.selectByAccount(account);
        if (user.getBotQQ() == null){
            return Result.error("请先绑定BotQQ");
        }
        Integer hostPort = dockerService.createContainer(user,napcatToken);
        return Result.success("创建容器成功,请根据端口访问napcatUI进行扫码登录", hostPort);
    }

    /**
     * 删除容器
     * @param token 用户设置的令牌
     */
    @RequestMapping("/delete")
    public Result<String> deleteContainer(@RequestHeader("Authorization") String token) {
        String account = jwtUtil.getAccountFromToken(token);
        User user = userService.selectByAccount(account);
        dockerService.deleteContainer(user.getQQ());
        return Result.success();
    }
}
