package com.example.demo.controller;

import com.example.demo.pojo.entity.BotInfo;
import com.example.demo.pojo.entity.Result;
import com.example.demo.service.BotService;
import com.example.demo.util.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bot")
public class BotController {

    private final BotService botService;
    private final AuthUtil authUtil;

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
    @PostMapping
    public Result<?> insert(HttpServletRequest request, String name, Long botQQ) {
        String userId = authUtil.getCurrentUserId(request);
        return botService.insert(userId, name, botQQ);
    }

    /**
     * 删除机器人
     * @param botQQ HTTP请求
     * @return 删除结果
     */
    @DeleteMapping
    public Result<?> delete(Long botQQ) {
        botService.delete(botQQ);
        return Result.success(null,null);
    }

    /**
     * 更新机器人信息
     * @param botInfo bot实体类信息
     * @return 更新结果
     */
    @PutMapping
    public Result<?> update(@RequestBody BotInfo botInfo) {
        botService.update(botInfo);
        return Result.success(null,null);
    }


    /**
     * 获取机器人信息
     * @param request HTTP请求
     * @return 机器人信息
     */
    @GetMapping
    public Result<?> info(HttpServletRequest request) {
        String userId = authUtil.getCurrentUserId(request);
        return Result.success(botService.select(userId));
    }
}
