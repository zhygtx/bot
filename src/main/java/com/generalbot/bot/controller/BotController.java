package com.generalbot.bot.controller;

import com.generalbot.bot.entity.BotInfo;
import com.generalbot.common.api.Result;
import com.generalbot.security.UserPrincipal;
import com.generalbot.bot.service.BotService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bot")
public class BotController {

    private final BotService botService;

    @Value("${napcat.ws.server.url}")
    private String path;

    @Value("${app.ip}")
    private String ip;

    public BotController(BotService botService) {
        this.botService = botService;
    }

    /**
     * 插入机器人
     * @param request HTTP请求
     * @param name 机器人名称
     * @param botQQ 机器人QQ
     * @return 插入结果
     */
    @PostMapping
    public Result<?> insert(@AuthenticationPrincipal UserPrincipal user, String name, Long botQQ) {
        return botService.insert(user.userId(), name, botQQ);
    }

    /**
     * 删除机器人
     * @param botQQ HTTP请求
     * @return 删除结果
     */
    @DeleteMapping
    public Result<?> delete(Long botQQ) {
        botService.delete(botQQ);
        return Result.success("删除成功", null);
    }

    /**
     * 更新机器人信息
     * @param botInfo bot实体类信息
     * @return 更新结果
     */
    @PutMapping
    public Result<?> update(@RequestBody BotInfo botInfo) {
        botService.update(botInfo);
        return Result.success("更新成功",null);
    }


    /**
     * 获取机器人信息
     * @param request HTTP请求
     * @return 机器人信息
     */
    @GetMapping
    public Result<?> info(@AuthenticationPrincipal UserPrincipal user) {
        BotInfo botInfo = botService.select(user.userId());
        if ( botInfo != null){
            String pathSuffix = "ws://" + ip + path + "/" + botInfo.getPathSuffix();
            botInfo.setPathSuffix(pathSuffix);
        }
        return Result.success(null, botInfo);
    }
}
