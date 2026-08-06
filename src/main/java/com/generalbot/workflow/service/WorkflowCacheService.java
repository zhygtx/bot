package com.generalbot.workflow.service;

import com.generalbot.workflow.entity.WorkflowInfo;

import java.util.List;

/**
 * 工作流缓存服务接口。
 * <p>
 * Bot 事件工作流索引使用本地内存缓存，
 * 定时任务部分保留 Redis ZSet 做时间调度。
 */
public interface WorkflowCacheService {

    /** 添加工作流到本地缓存 */
    void addWorkflowToCache(WorkflowInfo workflowInfo);

    /** 从本地缓存移除工作流 */
    void removeWorkflowFromCache(String workflowId);

    /** 根据 botQQ 和 eventType 获取匹配的工作流（纯内存查询） */
    List<WorkflowInfo> getWorkflowsByBotEvent(Long botQQ, String eventType);

    /** 重建全部缓存（从数据库全量加载） */
    void reloadCache();

    /** 添加定时任务到 Redis ZSet */
    void addScheduledTask(WorkflowInfo workflowInfo);

    /** 从 Redis ZSet 移除定时任务 */
    void removeScheduledTask(String workflowId);

    /** 获取所有定时任务 */
    List<WorkflowInfo> getScheduledTasks();

    /** 根据插件 ID 移除相关的工作流缓存和定时任务 */
    void removeWorkflowsByPluginId(String pluginId);
}
