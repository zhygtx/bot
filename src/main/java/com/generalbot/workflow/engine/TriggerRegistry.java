package com.generalbot.workflow.engine;

import com.generalbot.workflow.entity.WorkflowInfo;
import com.generalbot.workflow.mapper.WorkflowInfoMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledFuture;

/**
 * 统一触发注册表。BOT 事件、定时任务都通过 triggerKey 查找并触发工作流。
 */
@Slf4j
@Component
public class TriggerRegistry {

    private final WorkflowInfoMapper workflowInfoMapper;
    private final WorkflowEngine workflowEngine;
    private final TaskScheduler taskScheduler;

    private final Map<String, Set<String>> triggerToWorkflowIds = new ConcurrentHashMap<>();
    private final Map<String, WorkflowInfo> workflowCache = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> scheduledFutures = new ConcurrentHashMap<>();

    public TriggerRegistry(WorkflowInfoMapper workflowInfoMapper,
                           @Lazy WorkflowEngine workflowEngine,
                           TaskScheduler taskScheduler) {
        this.workflowInfoMapper = workflowInfoMapper;
        this.workflowEngine = workflowEngine;
        this.taskScheduler = taskScheduler;
    }

    /**
     * 启动时注册所有启用且可用的工作流。
     */
    @PostConstruct
    public void init() {
        List<WorkflowInfo> workflows = workflowInfoMapper.selectEnabled();
        for (WorkflowInfo workflow : workflows) {
            register(workflow);
        }
        log.info("TriggerRegistry 初始化完成，共注册 {} 个工作流", workflowCache.size());
    }

    /**
     * 注册工作流到触发注册表。
     * @param workflowInfo 工作流信息
     */
    public void register(WorkflowInfo workflowInfo) {
        if (workflowInfo == null
                || !Boolean.TRUE.equals(workflowInfo.getEnabled())
                || !Boolean.TRUE.equals(workflowInfo.getAvailable())
                || workflowInfo.getTriggerKey() == null
                || workflowInfo.getTriggerKey().isBlank()) {
            return;
        }
        unregister(workflowInfo.getId());
        workflowCache.put(workflowInfo.getId(), workflowInfo);

        String triggerKey = workflowInfo.getTriggerKey();
        if (triggerKey.startsWith("schedule:")) {
            String scheduleValue = triggerKey.substring("schedule:".length());
            CronTrigger cronTrigger = new CronTrigger(scheduleValue);
            ScheduledFuture<?> future = taskScheduler.schedule(
                    () -> executeScheduled(workflowInfo), cronTrigger);
            if (future == null) {
                log.warn("工作流 {} 定时注册失败：{}", workflowInfo.getId(), triggerKey);
                return;
            }
            scheduledFutures.put(workflowInfo.getId(), future);
            log.info("工作流 {} 已注册 Cron 定时触发：{}", workflowInfo.getId(), scheduleValue);
        } else {
            triggerToWorkflowIds.computeIfAbsent(triggerKey, key -> new CopyOnWriteArraySet<>())
                    .add(workflowInfo.getId());
            log.info("工作流 {} 已注册触发键 {}", workflowInfo.getId(), triggerKey);
        }
    }

    /**
     * 取消注册工作流。
     * @param workflowId 工作流ID
     */
    public void unregister(String workflowId) {
        WorkflowInfo removed = workflowCache.remove(workflowId);
        if (removed != null) {
            triggerToWorkflowIds.values().forEach(ids -> ids.remove(workflowId));
        }
        ScheduledFuture<?> future = scheduledFutures.remove(workflowId);
        if (future != null) {
            future.cancel(false);
        }
    }

    /**
     * 触发指定 triggerKey 下的全部工作流，支持同一事件链去重。
     * @param triggerKey 触发键
     * @param payload 触发载荷
     * @param alreadyExecuted 已执行的工作流ID集合，可为 null
     */
    public void fire(String triggerKey, Object payload, Set<String> alreadyExecuted) {
        Set<String> workflowIds = triggerToWorkflowIds.get(triggerKey);
        if (workflowIds == null || workflowIds.isEmpty()) {
            return;
        }
        for (String workflowId : workflowIds) {
            if (alreadyExecuted != null && !alreadyExecuted.add(workflowId)) {
                continue;
            }
            WorkflowInfo workflowInfo = workflowCache.get(workflowId);
            if (workflowInfo == null) {
                continue;
            }
            try {
                workflowEngine.execute(workflowInfo, triggerKey, payload);
            } catch (Exception e) {
                log.error("工作流 {} 触发执行失败：{}", workflowId, e.getMessage());
            }
        }
    }

    private void executeScheduled(WorkflowInfo workflowInfo) {
        if (!workflowCache.containsKey(workflowInfo.getId())) {
            return;
        }
        Map<String, Object> payload = Map.of("triggerTime", System.currentTimeMillis());
        try {
            workflowEngine.execute(workflowInfo, workflowInfo.getTriggerKey(), payload);
        } catch (Exception e) {
            log.error("定时工作流 {} 执行失败：{}", workflowInfo.getId(), e.getMessage());
        }
    }

}
