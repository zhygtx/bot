package com.example.demo.controller;

import com.example.demo.docker.DockerService;
import com.example.demo.pojo.Result;
import com.example.demo.pojo.User;
import com.example.demo.service.UserService;
import com.example.demo.utils.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/docker")
public class DockerController {

    private final DockerService dockerService;
    private final UserService userService;
    private final AuthUtil authUtil;

    @Autowired
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
    public Result<Integer> createContainer(HttpServletRequest request, String napcatToken) {
        String account = authUtil.getCurrentUserId(request);
        User user = userService.selectByAccount(account);
        if (user.getBotQQ() == null){
            return Result.error("请先绑定BotQQ");
        }
        Integer hostPort = dockerService.createContainer(user,napcatToken);
        return Result.success("创建容器成功,请根据端口访问napcatUI进行扫码登录", hostPort);
    }

    /**
     * 删除容器
     * @param request HTTP请求
     */
    @RequestMapping("/delete")
    public Result<String> deleteContainer(HttpServletRequest request) {
        String account = authUtil.getCurrentUserId(request);
        User user = userService.selectByAccount(account);
        dockerService.deleteContainer(user.getQQ());
        return Result.success();
    }
}
