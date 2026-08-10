package com.generalbot.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 数据统计首页的聚合响应。
 * GET /statistics/overview 一次返回首页需要的全部数据，前端不再拼多个接口。
 */
@Data
public class StatisticsOverview {

    /**
     * 当前用户自己的 Bot 在线状态与本次在线时长
     */
    private BotStat bot;

    /**
     * 资源数量汇总：插件、工作流
     */
    private ResourceStat resource;

    /**
     * 今日执行汇总
     */
    private ExecutionSummary today;

    /**
     * 执行趋势（默认 7 天，可切换 15 天），按日期升序，缺数据的日期补 0，保证折线图连续
     */
    private List<ExecutionTrendItem> trend;

    /**
     * Top 工作流（按总执行次数倒序），合并了每日汇总表和今日原始明细
     */
    private List<TopWorkflowItem> topWorkflows;

    /**
     * Bot 在线状态项。
     */
    @Data
    @AllArgsConstructor
    public static class BotStat {

        /**
         * 机器人QQ
         */
        private Long botQQ;

        /**
         * 机器人名称
         */
        private String name;

        /**
         * 是否在线
         */
        private boolean online;

        /**
         * 本次上线时间戳（毫秒），离线时为 null
         */
        private Long onlineSince;

        /**
         * 本次在线时长（毫秒），在线时为“当前时间 - onlineSince”，离线时为 null
         */
        private Long onlineDurationMs;
    }

    /**
     * 资源数量汇总项。
     */
    @Data
    @AllArgsConstructor
    public static class ResourceStat {

        /**
         * 插件总数
         */
        private long pluginCount;

        /**
         * 公开插件数
         */
        private long publicPluginCount;

        /**
         * 工作流总数
         */
        private long workflowCount;

        /**
         * 启用中的工作流数
         */
        private long enabledWorkflowCount;

    }

    /**
     * 今日执行汇总项。
     */
    @Data
    @AllArgsConstructor
    public static class ExecutionSummary {

        /**
         * 今日执行次数
         */
        private long executeCount;

        /**
         * 今日实际执行过的工作流数量，反映执行覆盖范围
         */
        private long workflowCount;

        /**
         * 今日平均耗时（毫秒）
         */
        private long avgDurationMs;

        /**
         * 今日平均实际执行节点数
         */
        private BigDecimal avgNodeCount;
    }

    /**
     * 单日执行趋势项。
     */
    @Data
    @AllArgsConstructor
    public static class ExecutionTrendItem {

        /**
         * 统计日期
         */
        private LocalDate date;

        /**
         * 当日执行次数
         */
        private long executeCount;

        /**
         * 当日平均耗时（毫秒）
         */
        private long avgDurationMs;

        /**
         * 当日平均实际执行节点数
         */
        private double avgNodeCount;
    }

    /**
     * Top 工作流项。
     */
    @Data
    @AllArgsConstructor
    public static class TopWorkflowItem {

        /**
         * 工作流ID
         */
        private String workflowId;

        /**
         * 工作流名称（汇总时取最新快照）
         */
        private String workflowName;

        /**
         * 累计执行次数
         */
        private long executeCount;

        /**
         * 平均耗时（毫秒），跨日合并时用总耗时/总次数重新计算
         */
        private long avgDurationMs;

        /**
         * 平均实际执行节点数，跨日合并时用总节点数/总次数重新计算
         */
        private double avgNodeCount;

        /**
         * 累计总耗时（毫秒），用于合并今日明细时计算加权平均
         */
        private long totalDurationMs;

        /**
         * 累计实际执行节点总数，用于合并今日明细时计算加权平均
         */
        private long totalNodeCount;

        /**
         * 最近一次执行开始时间戳（毫秒），历史数据来自每日汇总表的 last_start_time
         */
        private Long lastExecutionTime;
    }

}
