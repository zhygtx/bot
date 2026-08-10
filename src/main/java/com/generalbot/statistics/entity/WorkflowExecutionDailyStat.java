package com.generalbot.statistics.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 工作流执行日志每日统计实体。
 * 每天凌晨由定时任务把前一天 workflow_execution 的明细按“日期 + 用户 + 工作流”聚合到这里；
 * 原始日志超过 7 天被删除后，历史趋势和排行仍可以从这张表查询。
 */
@Data
public class WorkflowExecutionDailyStat {

    /**
     * 统计记录ID（自增主键）
     */
    private Long id;

    /**
     * 统计日期，取服务器本地日期，避免跨时区时把同一天的执行拆成两行
     */
    private LocalDate statDate;

    /**
     * 用户ID，统计首页按当前登录用户过滤
     */
    private String userId;

    /**
     * 工作流ID
     */
    private String workflowId;

    /**
     * 工作流名称快照：聚合时写入当天名称，工作流以后改名不影响历史统计展示
     */
    private String workflowName;

    /**
     * 当日执行次数
     */
    private Integer executeCount;

    /**
     * 当日成功次数
     */
    private Integer successCount;

    /**
     * 当日失败次数
     */
    private Integer failedCount;

    /**
     * 当日总耗时（毫秒），用于跨日合并时重新计算加权平均耗时
     */
    private Long totalDurationMs;

    /**
     * 当日平均耗时（毫秒）
     */
    private Long avgDurationMs;

    /**
     * 当日最大耗时（毫秒）
     */
    private Long maxDurationMs;

    /**
     * 当日最小耗时（毫秒）
     */
    private Long minDurationMs;

    /**
     * 当日实际执行节点总数
     */
    private Long totalActualNodeCount;

    /**
     * 当日平均实际执行节点数
     */
    private BigDecimal avgActualNodeCount;

    /**
     * 当日最后一次执行开始时间戳（毫秒），Top 工作流展示“最近执行时间”用
     */
    private Long lastStartTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后更新时间，重复执行同一天汇总时由 ON DUPLICATE KEY UPDATE 自动刷新
     */
    private LocalDateTime updateTime;
}
