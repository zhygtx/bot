package com.example.demo.controller;

import com.example.demo.pojo.Result;
import com.example.demo.service.BotService;
import com.example.demo.utils.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bot")
public class BotController {

    private final BotService botService;
    private final AuthUtil authUtil;

    @Autowired
    public BotController(BotService botService, AuthUtil authUtil) {
        this.botService = botService;
        this.authUtil = authUtil;
    }

    /**
     * 插入机器人
     * @param request HTTP请求
     * @param name 机器人名称
     * @param botQQ 机器人QQ
     * @return 插入结果
     */
    @RequestMapping("/insert")
    public Result<?> insert(HttpServletRequest request, String name, Long botQQ) {
        String userId = authUtil.getCurrentUserId(request);
        botService.insert(userId, name, botQQ);
        return Result.success();
    }

    /**
     * 获取机器人信息
     * @param request HTTP请求
     * @return 机器人信息
     */
    @RequestMapping("/info")
    public Result<?> info(HttpServletRequest request) {
        String userId = authUtil.getCurrentUserId(request);
        return Result.success(botService.select(userId));
    }

    /**
     * 删除机器人
     * @param request HTTP请求
     * @return 删除结果
     */
    @RequestMapping("/delete")
    public Result<?> delete(HttpServletRequest request) {
        String userId = authUtil.getCurrentUserId(request);
        botService.delete(userId);
        return Result.success();
    }

    /**
     * 更新机器人信息
     * @param request HTTP请求
     * @param name 机器人名称
     * @param botQQ 机器人QQ
     * @return 更新结果
     */
    @RequestMapping("/update")
    public Result<?> update(HttpServletRequest request, String name, Long botQQ) {
        String userId = authUtil.getCurrentUserId(request);
        botService.update(userId, name, botQQ);
        return Result.success();
    }
}
