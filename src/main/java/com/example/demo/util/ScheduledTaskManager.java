package com.example.demo.util;

import com.example.demo.pojo.workflow.WorkflowInfo;
import com.example.demo.service.RedisWorkflowService;
import com.example.demo.service.WorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * 定时任务管理器，用于管理工作流的定时执行
 */
@Component
@Slf4j
public class ScheduledTaskManager {

    private final RedisWorkflowService redisWorkflowService;
    private final WorkflowService workflowService;
    private final WorkflowUtil workflowUtil;

    @Autowired
    public ScheduledTaskManager(RedisWorkflowService redisWorkflowService, 
                               @org.springframework.context.annotation.Lazy WorkflowService workflowService, 
                               WorkflowUtil workflowUtil) {
        this.redisWorkflowService = redisWorkflowService;
        this.workflowService = workflowService;
        this.workflowUtil = workflowUtil;
    }

    /**
     * 初始化定时任务
     */
    @PostConstruct
    public void init() {
        try {
            // 扫描所有工作流，初始化定时任务
            log.info("开始初始化定时任务...");
            com.github.pagehelper.PageInfo<WorkflowInfo> pageInfo = workflowService.findAll(null,1, Integer.MAX_VALUE);
            for (WorkflowInfo workflow : pageInfo.getList()) {
                redisWorkflowService.addScheduledTask(workflow);
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
            // 获取所有定时任务
            List<WorkflowInfo> allTasks = redisWorkflowService.getScheduledTasks();
            
            if (allTasks.isEmpty()) {
                return;
            }
            
            log.info("发现 {} 个定时任务", allTasks.size());
            
            for (WorkflowInfo workflow : allTasks) {
                String workflowId = workflow.getId();
                try {
                    // 执行工作流
                    executeWorkflow(workflow);
                    // 执行成功，重新添加定时任务（更新下次执行时间）
                    redisWorkflowService.addScheduledTask(workflow);
                } catch (Exception e) {
                    log.error("执行定时任务失败：workflowId={}", workflowId, e);
                    // 执行出错时移除定时任务
                    redisWorkflowService.removeScheduledTask(workflowId);
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
