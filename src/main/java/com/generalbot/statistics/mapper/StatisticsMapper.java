package com.generalbot.statistics.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 数据统计 Mapper。
 * 同时承担两类职责：数据首页的跨表聚合查询，以及凌晨“汇总前一天 + 清理 7 天前明细”的维护任务。
 * 跨表只读统计放在这里，避免在每个业务 Mapper 里重复加统计方法。
 */
@Mapper
public interface StatisticsMapper {

    /**
     * 把指定时间区间内的 workflow_execution 明细按“用户 + 工作流”聚合到每日统计表。
     * 同一日期重复执行时通过唯一索引 + ON DUPLICATE KEY UPDATE 覆盖更新，天然幂等。
     * @param statDate 统计日期
     * @param dayStart 区间开始时间戳（毫秒，含）
     * @param dayEnd 区间结束时间戳（毫秒，不含）
     * @return 受影响行数
     */
    int insertDailyStat(@Param("statDate") LocalDate statDate,
                        @Param("dayStart") long dayStart,
                        @Param("dayEnd") long dayEnd);

    /**
     * 删除指定时间戳之前的执行明细。
     * 删除前必须先完成每日汇总，否则被删日志的统计就永久丢失。
     * @param cutoff 清理截止时间戳（毫秒），start_time 小于该值的记录会被删除
     * @return 删除的记录数
     */
    int deleteExpiredExecutions(@Param("cutoff") long cutoff);

    /**
     * 查询当前用户的 Bot 在线状态和本次上线时间，统计首页“我的 BOT”卡片使用。
     * @param userId 用户ID
     * @return 单条 Bot 统计，无 Bot 时返回 null
     */
    Map<String, Object> selectBotStat(@Param("userId") String userId);

    /**
     * 查询当前用户的资源数量：插件、工作流。
     * AI/Bot 相关子查询已移除，首页不需要展示这两类卡片。
     * @param userId 用户ID
     * @return 各资源计数
     */
    Map<String, Object> selectResourceStat(@Param("userId") String userId);

    /**
     * 查询今天（指定毫秒区间）的执行汇总，包含执行次数、涉及工作流数、平均耗时和平均节点数。
     * @param userId 用户ID
     * @param dayStart 今天开始时间戳（毫秒，含）
     * @param dayEnd 明天开始时间戳（毫秒，不含）
     * @return 今日执行汇总
     */
    Map<String, Object> selectTodayExecutionStat(@Param("userId") String userId,
                                                 @Param("dayStart") long dayStart,
                                                 @Param("dayEnd") long dayEnd);

    /**
     * 查询每日统计表在指定日期区间内的聚合行。
     * 这是 7 天清理后仍能展示历史趋势的数据来源。
     * @param userId 用户ID
     * @param startDate 开始日期（含）
     * @param endDate 结束日期（含）
     * @return 每日统计行（执行次数、平均耗时、平均节点数）
     */
    List<Map<String, Object>> selectDailyStat(@Param("userId") String userId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    /**
     * 直接按原始执行明细按天聚合。
     * 只用于补齐“每日汇总表还没生成”的日期（例如今天或任务漏跑的昨天），
     * 与每日统计表按日期合并时以汇总表优先。
     * @param userId 用户ID
     * @param startTime 开始时间戳（毫秒，含）
     * @param endTime 结束时间戳（毫秒，不含）
     * @return 按天聚合的执行统计
     */
    List<Map<String, Object>> selectRawExecutionDayStat(@Param("userId") String userId,
                                                        @Param("startTime") long startTime,
                                                        @Param("endTime") long endTime);

    /**
     * 从每日统计表聚合每个工作流的累计数据（不含今天）。
     * @param userId 用户ID
     * @return 工作流累计统计
     */
    List<Map<String, Object>> selectTopWorkflowStat(@Param("userId") String userId);

    /**
     * 从原始执行明细聚合今天的每个工作流数据，用于和每日统计表合并成 Top 工作流。
     * @param userId 用户ID
     * @param dayStart 今天开始时间戳（毫秒，含）
     * @param dayEnd 明天开始时间戳（毫秒，不含）
     * @return 今日工作流统计
     */
    List<Map<String, Object>> selectTodayTopWorkflowStat(@Param("userId") String userId,
                                                        @Param("dayStart") long dayStart,
                                                        @Param("dayEnd") long dayEnd);

}
