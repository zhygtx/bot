package com.generalbot.statistics.service.impl;

import com.generalbot.statistics.dto.StatisticsOverview;
import com.generalbot.statistics.mapper.StatisticsMapper;
import com.generalbot.statistics.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 数据统计服务实现。
 * 首页数据来源分两层：最近 7 天直接读原始 workflow_execution，
 * 更早历史读 workflow_execution_daily_stat；两层按日期/工作流合并，
 * 保证凌晨清理原始明细后首页趋势和排行仍然完整。
 */
@Slf4j
@Service
public class StatisticsServiceImpl implements StatisticsService {

    /**
     * Top 工作流最多展示多少条
     */
    private static final int TOP_WORKFLOW_LIMIT = 10;

    private final StatisticsMapper statisticsMapper;

    /**
     * 构造统计服务。
     * @param statisticsMapper 统计 Mapper
     */
    public StatisticsServiceImpl(StatisticsMapper statisticsMapper) {
        this.statisticsMapper = statisticsMapper;
    }

    /**
     * 组装统计首页总览。
     * 顺序：Bot 状态 -> 资源数量 -> 今日汇总 -> 执行趋势 -> Top 工作流。
     * @param userId 当前登录用户ID
     * @param trendDays 趋势图展示天数（7 或 15），必须大于 0
     * @return 首页聚合数据
     */
    @Override
    public StatisticsOverview overview(String userId, int trendDays) {
        if (trendDays <= 0) {
            throw new IllegalArgumentException("趋势天数必须大于 0");
        }
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);
        LocalDate trendStart = today.minusDays(trendDays - 1L);

        // 今天的毫秒边界：统计“今天 00:00 到明天 00:00”的执行记录
        long dayStart = today.atStartOfDay(zone).toInstant().toEpochMilli();
        long dayEnd = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli();

        StatisticsOverview overview = new StatisticsOverview();
        overview.setBot(buildBotStat(userId));
        overview.setResource(buildResourceStat(userId));
        overview.setToday(buildTodaySummary(userId, dayStart, dayEnd));
        overview.setTrend(buildTrend(userId, trendStart, today, dayStart, dayEnd, trendDays));
        overview.setTopWorkflows(buildTopWorkflows(userId, dayStart, dayEnd));
        return overview;
    }

    /**
     * 构建“我的 BOT”在线状态：在线时用当前时间减去 online_since 得到本次在线时长。
     * @param userId 用户ID
     * @return Bot 统计，用户没有登记 Bot 时返回 null
     */
    private StatisticsOverview.BotStat buildBotStat(String userId) {
        Map<String, Object> row = statisticsMapper.selectBotStat(userId);
        if (row == null || row.get("botQQ") == null) {
            return null;
        }
        boolean online = toBool(row.get("online"));
        Long onlineSince = toLong(row.get("onlineSince"));
        Long onlineDurationMs = online && onlineSince != null
                ? System.currentTimeMillis() - onlineSince
                : null;
        return new StatisticsOverview.BotStat(
                toLong(row.get("botQQ")),
                row.get("name") == null ? "" : String.valueOf(row.get("name")),
                online,
                onlineSince,
                onlineDurationMs
        );
    }

    /**
     * 构建资源数量：Bot、插件、工作流、AI 会话都由 StatisticsMapper 一次查出。
     * 首页只展示插件和工作流。
     * @param userId 用户ID
     * @return 资源数量汇总
     */
    private StatisticsOverview.ResourceStat buildResourceStat(String userId) {
        Map<String, Object> row = statisticsMapper.selectResourceStat(userId);
        return new StatisticsOverview.ResourceStat(
                toLong(row.get("pluginCount")),
                toLong(row.get("publicPluginCount")),
                toLong(row.get("workflowCount")),
                toLong(row.get("enabledWorkflowCount"))
        );
    }

    /**
     * 构建今日执行汇总；没有执行记录时各计数为 0。
     * 指标聚焦执行次数、涉及工作流数、平均耗时和平均节点数，不展示失败相关数据。
     * @param userId 用户ID
     * @param dayStart 今天开始毫秒
     * @param dayEnd 明天开始毫秒
     * @return 今日执行汇总
     */
    private StatisticsOverview.ExecutionSummary buildTodaySummary(String userId, long dayStart, long dayEnd) {
        Map<String, Object> row = statisticsMapper.selectTodayExecutionStat(userId, dayStart, dayEnd);
        return new StatisticsOverview.ExecutionSummary(
                toLong(row.get("executeCount")),
                toLong(row.get("workflowCount")),
                toLong(row.get("avgDurationMs")),
                toBigDecimal(row.get("avgNodeCount"))
        );
    }

    /**
     * 构建执行趋势（支持 7 天或 15 天）。
     * 先放每日统计表（历史部分），再补原始明细里“统计表还没有的日期”（通常是今天或任务漏跑的昨天），
     * 最后给没有数据的日期补 0，保证折线图每天都有一点。
     * 趋势只保留执行次数、平均耗时和平均节点数。
     * @param userId 用户ID
     * @param trendStart 趋势开始日期
     * @param today 今天
     * @param dayStart 今天开始毫秒
     * @param dayEnd 明天开始毫秒
     * @param trendDays 趋势图展示天数
     * @return 按日期升序的趋势列表
     */
    private List<StatisticsOverview.ExecutionTrendItem> buildTrend(String userId,
                                                                   LocalDate trendStart,
                                                                   LocalDate today,
                                                                   long dayStart,
                                                                   long dayEnd,
                                                                   int trendDays) {
        ZoneId zone = ZoneId.systemDefault();
        Map<LocalDate, StatisticsOverview.ExecutionTrendItem> trendMap = new TreeMap<>();

        for (Map<String, Object> row : statisticsMapper.selectDailyStat(userId, trendStart, today)) {
            LocalDate date = LocalDate.parse(String.valueOf(row.get("statDate")));
            trendMap.put(date, new StatisticsOverview.ExecutionTrendItem(
                    date,
                    toLong(row.get("executeCount")),
                    toLong(row.get("avgDurationMs")),
                    toDouble(row.get("avgNodeCount"))
            ));
        }

        long windowStart = trendStart.atStartOfDay(zone).toInstant().toEpochMilli();
        for (Map<String, Object> row : statisticsMapper.selectRawExecutionDayStat(userId, windowStart, dayEnd)) {
            LocalDate date = LocalDate.parse(String.valueOf(row.get("statDate")));
            // 每日统计表已有该日期时以汇总表为准，避免同一天重复叠加
            if (!trendMap.containsKey(date)) {
                trendMap.put(date, new StatisticsOverview.ExecutionTrendItem(
                        date,
                        toLong(row.get("executeCount")),
                        toLong(row.get("avgDurationMs")),
                        toDouble(row.get("avgNodeCount"))
                ));
            }
        }

        for (int i = 0; i < trendDays; i++) {
            LocalDate date = trendStart.plusDays(i);
            trendMap.computeIfAbsent(date, d -> new StatisticsOverview.ExecutionTrendItem(d, 0, 0, 0));
        }
        return new ArrayList<>(trendMap.values());
    }

    /**
     * 构建 Top 工作流。
     * 每日汇总表提供清理后的历史累计，今天原始明细还没进汇总表，单独查询后合并；
     * 平均耗时和平均节点数都用“总量/总次数”重新计算，避免简单平均多个日均值造成权重失真。
     * @param userId 用户ID
     * @param dayStart 今天开始毫秒
     * @param dayEnd 明天开始毫秒
     * @return 按执行次数倒序的 Top 工作流
     */
    private List<StatisticsOverview.TopWorkflowItem> buildTopWorkflows(String userId, long dayStart, long dayEnd) {
        Map<String, StatisticsOverview.TopWorkflowItem> workflowMap = new LinkedHashMap<>();

        for (Map<String, Object> row : statisticsMapper.selectTopWorkflowStat(userId)) {
            String workflowId = String.valueOf(row.get("workflowId"));
            long executeCount = toLong(row.get("executeCount"));
            long totalDurationMs = toLong(row.get("totalDurationMs"));
            long totalNodeCount = toLong(row.get("totalNodeCount"));
            long avgDurationMs = executeCount == 0 ? 0 : totalDurationMs / executeCount;
            double avgNodeCount = executeCount == 0 ? 0 : (double) totalNodeCount / executeCount;
            Long lastExecutionTime = row.get("lastExecutionTime") == null
                    ? null
                    : toLong(row.get("lastExecutionTime"));
            workflowMap.put(workflowId, new StatisticsOverview.TopWorkflowItem(
                    workflowId,
                    row.get("workflowName") == null ? "" : String.valueOf(row.get("workflowName")),
                    executeCount,
                    avgDurationMs,
                    avgNodeCount,
                    totalDurationMs,
                    totalNodeCount,
                    lastExecutionTime
            ));
        }

        for (Map<String, Object> row : statisticsMapper.selectTodayTopWorkflowStat(userId, dayStart, dayEnd)) {
            String workflowId = String.valueOf(row.get("workflowId"));
            long executeCount = toLong(row.get("executeCount"));
            long totalDurationMs = toLong(row.get("totalDurationMs"));
            long totalNodeCount = toLong(row.get("totalNodeCount"));
            long avgDurationMs = executeCount == 0 ? 0 : totalDurationMs / executeCount;
            double avgNodeCount = executeCount == 0 ? 0 : (double) totalNodeCount / executeCount;
            Long lastExecutionTime = toLong(row.get("lastExecutionTime"));

            StatisticsOverview.TopWorkflowItem existing = workflowMap.get(workflowId);
            if (existing == null) {
                workflowMap.put(workflowId, new StatisticsOverview.TopWorkflowItem(
                        workflowId,
                        row.get("workflowName") == null ? "" : String.valueOf(row.get("workflowName")),
                        executeCount,
                        avgDurationMs,
                        avgNodeCount,
                        totalDurationMs,
                        totalNodeCount,
                        lastExecutionTime
                ));
                continue;
            }

            long mergedExecute = existing.getExecuteCount() + executeCount;
            long mergedTotalDuration = existing.getTotalDurationMs() + totalDurationMs;
            long mergedTotalNodeCount = existing.getTotalNodeCount() + totalNodeCount;
            existing.setExecuteCount(mergedExecute);
            existing.setTotalDurationMs(mergedTotalDuration);
            existing.setTotalNodeCount(mergedTotalNodeCount);
            existing.setAvgDurationMs(mergedExecute == 0 ? 0 : mergedTotalDuration / mergedExecute);
            existing.setAvgNodeCount(mergedExecute == 0 ? 0 : (double) mergedTotalNodeCount / mergedExecute);
            if (existing.getLastExecutionTime() == null
                    || (lastExecutionTime != null && lastExecutionTime > existing.getLastExecutionTime())) {
                existing.setLastExecutionTime(lastExecutionTime);
            }
        }

        List<StatisticsOverview.TopWorkflowItem> topWorkflows = new ArrayList<>(workflowMap.values());
        topWorkflows.sort(Comparator.comparingLong(StatisticsOverview.TopWorkflowItem::getExecuteCount).reversed());
        return topWorkflows.size() > TOP_WORKFLOW_LIMIT
                ? new ArrayList<>(topWorkflows.subList(0, TOP_WORKFLOW_LIMIT))
                : topWorkflows;
    }

    /**
     * 数据库计数转 long；SQL 里 SUM/COUNT 在无数据或类型不同时可能返回 null 或不同数值类型。
     * @param value 数据库返回的数值
     * @return long 值
     */
    private long toLong(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }

    /**
     * 数据库小数或数值转 double，用于平均耗时、平均节点数等带小数的统计字段。
     * @param value 数据库返回的数值
     * @return double 值
     */
    private double toDouble(Object value) {
        return value == null ? 0D : ((Number) value).doubleValue();
    }

    /**
     * 数据库小数转 BigDecimal，默认保留两位小数。
     * @param value 数据库返回的数值
     * @return BigDecimal 值
     */
    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2);
        }
        return new BigDecimal(value.toString()).setScale(2);
    }

    /**
     * MySQL TINYINT(1) 可能被驱动映射成 Boolean、Integer 或 Byte，统一转成 boolean。
     * @param value 数据库返回的在线标记
     * @return boolean 值
     */
    private boolean toBool(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return value instanceof Number && ((Number) value).intValue() == 1;
    }
}
