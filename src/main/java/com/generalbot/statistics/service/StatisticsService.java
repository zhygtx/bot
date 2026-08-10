package com.generalbot.statistics.service;

import com.generalbot.statistics.dto.StatisticsOverview;

/**
 * 数据统计服务接口。
 * 负责把 Bot、插件、工作流、执行日志、AI 会话等数据组装成统计首页所需的结构。
 */
public interface StatisticsService {

    /**
     * 获取当前用户的统计数据首页总览。
     * @param userId 当前登录用户ID
     * @param trendDays 趋势图展示天数，前端支持 7 天和 15 天切换
     * @return 首页聚合数据
     */
    StatisticsOverview overview(String userId, int trendDays);
}
