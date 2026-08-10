package com.generalbot.statistics.controller;

import com.generalbot.common.api.Result;
import com.generalbot.security.UserPrincipal;
import com.generalbot.statistics.dto.StatisticsOverview;
import com.generalbot.statistics.service.StatisticsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据统计控制器。
 * 提供统计首页所需的聚合数据，所有查询都按当前登录用户隔离。
 */
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 构造统计控制器。
     * @param statisticsService 统计服务
     */
    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * 获取统计首页总览。
     * 一次返回 Bot 状态、资源数量、今日汇总、执行趋势、Top 工作流。
     * @param user 当前登录用户（JWT 认证后由 Spring Security 注入）
     * @param days 趋势图展示天数，默认 7，前端可切换 7/15
     * @return 统计首页聚合数据
     */
    @GetMapping("/overview")
    public Result<?> overview(@AuthenticationPrincipal UserPrincipal user,
                              @RequestParam(defaultValue = "7") int days) {
        StatisticsOverview overview = statisticsService.overview(user.userId(), days);
        return Result.success(null, overview);
    }
}
