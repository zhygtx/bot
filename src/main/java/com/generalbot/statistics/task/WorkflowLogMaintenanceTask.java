package com.generalbot.statistics.task;

import com.generalbot.statistics.mapper.StatisticsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * 执行日志凌晨维护任务。
 * 每天 03:30 做两件事：先把前一天的工作流执行明细聚合进每日统计表，
 * 再删除超过 7 天的原始明细。两步放在同一个事务里，任一失败都整体回滚，
 * 第二天重跑仍能补齐前一天统计，不会出现“明细删了但统计没落库”的缺口。
 */
@Slf4j
@Component
public class WorkflowLogMaintenanceTask {

    /**
     * 原始执行日志保留天数；超过该天数的明细会在每日维护时删除
     */
    private static final int EXECUTION_LOG_RETENTION_DAYS = 7;

    /**
     * 维护任务使用的统计日期基准：取服务器本地时区
     */
    private static final ZoneId ZONE = ZoneId.systemDefault();

    private final StatisticsMapper statisticsMapper;

    /**
     * 构造维护任务。
     * @param statisticsMapper 统计 Mapper，汇总和删除 SQL 都在这一个 Mapper 里
     */
    public WorkflowLogMaintenanceTask(StatisticsMapper statisticsMapper) {
        this.statisticsMapper = statisticsMapper;
    }

    /**
     * 每天凌晨 03:30 执行日志维护。
     * 先汇总前一天（昨天 00:00 到今天 00:00），再删除 7 天前零点之前的所有明细；
     * 汇总在前是因为一旦原始行被删，这些日志的统计就再也无法从明细恢复。
     */
    @Scheduled(cron = "0 30 3 * * ?")
    @Transactional
    public void cleanAndSummarizeLogs() {
        LocalDate today = LocalDate.now(ZONE);
        LocalDate yesterday = today.minusDays(1);
        long dayStart = yesterday.atStartOfDay(ZONE).toInstant().toEpochMilli();
        long dayEnd = today.atStartOfDay(ZONE).toInstant().toEpochMilli();
        long cutoff = today.minusDays(EXECUTION_LOG_RETENTION_DAYS).atStartOfDay(ZONE).toInstant().toEpochMilli();

        int summarizedRows = statisticsMapper.insertDailyStat(yesterday, dayStart, dayEnd);
        int deletedRows = statisticsMapper.deleteExpiredExecutions(cutoff);
        log.info("执行日志维护完成：汇总日期 {} 共 {} 行，清理 {} 条过期明细",
                yesterday, summarizedRows, deletedRows);
    }
}
