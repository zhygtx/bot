package com.generalbot.workflow.schedule;

import com.generalbot.workflow.entity.WorkflowInfo;
import com.generalbot.workflow.service.WorkflowCacheService;
import com.generalbot.workflow.service.WorkflowService;
import com.generalbot.workflow.engine.WorkflowUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 定时任务管理器，用于管理工作流的定时执行
 */
@Component
@Slf4j
public class ScheduledTaskManager {

    private final WorkflowCacheService workflowCacheService;
    private final WorkflowService workflowService;
    private final WorkflowUtil workflowUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    public ScheduledTaskManager(WorkflowCacheService workflowCacheService, 
                               @org.springframework.context.annotation.Lazy WorkflowService workflowService, 
                               WorkflowUtil workflowUtil,
                               RedisTemplate<String, Object> redisTemplate) {
        this.workflowCacheService = workflowCacheService;
        this.workflowService = workflowService;
        this.workflowUtil = workflowUtil;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 初始化定时任务
     */
    @PostConstruct
    public void init() {
        try {
            // 扫描所有工作流，初始化定时任务
            log.info("开始初始化定时任务...");
            List<WorkflowInfo> scheduledTasks = workflowService.findAllScheduledTask();
            for (WorkflowInfo workflow : scheduledTasks) {
                workflowCacheService.addScheduledTask(workflow);
            }
            log.info("定时任务初始化完成");
        } catch (Exception e) {
            log.error("初始化定时任务失败", e);
        }
    }

    /**
     * 扫描并执行过期任务
     */
    @Scheduled(cron = "0 * * * * ?") // 每分钟 执行一次
    public void scanAndExecuteTasks() {
        try {
            long now = System.currentTimeMillis();
            // 获取所有定时任务
            List<WorkflowInfo> allTasks = workflowCacheService.getScheduledTasks();
            
            if (allTasks.isEmpty()) {
                return;
            }
            
            log.info("发现 {} 个定时任务", allTasks.size());
            
            for (WorkflowInfo workflow : allTasks) {
                String workflowId = workflow.getId();
                try {
                    // 检查任务是否到期
                    Double score = redisTemplate.opsForZSet().score("scheduled_tasks", workflow);
                    if (score == null || score > now) {
                        log.debug("工作流 {} 尚未到期，跳过执行", workflowId);
                        continue;
                    }
                    
                    // 执行工作流
                    executeWorkflow(workflow);
                    // 检查工作流是否仍存在于定时任务中（节点执行失败时editDisableReason已将其从Redis移除）
                    if (redisTemplate.opsForZSet().score("scheduled_tasks", workflow) != null) {
                        // 执行成功，移除旧任务并重新添加（更新下次执行时间）
                        workflowCacheService.removeScheduledTask(workflowId);
                        workflowCacheService.addScheduledTask(workflow);
                    }
                } catch (Exception e) {
                    log.error("执行定时任务失败：workflowId={}", workflowId, e);
                    workflowService.editDisableReason(workflowId, "定时任务执行出错:" + e.getMessage());
                    // 执行出错时移除定时任务
                    workflowCacheService.removeScheduledTask(workflowId);
                }
            }
        } catch (Exception e) {
            log.error("扫描定时任务失败", e);
        }
    }

    /**
     * 执行工作流
     * @param workflow 工作流信息
     * @throws Exception 执行异常
     */
    private void executeWorkflow(WorkflowInfo workflow) throws Exception {
        if (workflow == null) {
            log.warn("工作流不存在");
            return;
        }

        String workflowId = workflow.getId();
        log.info("开始执行定时工作流: {} (ID: {})", workflow.getName(), workflowId);

        // 执行工作流，不需要传入上下文数据
        workflowUtil.executeWorkflow(workflow, "", null);
        
        log.info("定时工作流执行完成: {}", workflowId);
    }
}
