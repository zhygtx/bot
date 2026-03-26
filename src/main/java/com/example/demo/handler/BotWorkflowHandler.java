package com.example.demo.handler;

import com.example.demo.pojo.workflow.WorkflowInfo;
import com.example.demo.service.RedisWorkflowService;
import com.example.demo.util.WorkflowUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * BOT工作流处理器，用于处理BOT事件触发工作流的逻辑
 */
@Component
@Slf4j
public class BotWorkflowHandler {

    private final RedisWorkflowService redisWorkflowService;
    private final WorkflowUtil workflowUtil;

    public BotWorkflowHandler(RedisWorkflowService redisWorkflowService, WorkflowUtil workflowUtil) {
        this.redisWorkflowService = redisWorkflowService;
        this.workflowUtil = workflowUtil;
    }

    /**
     * 处理BOT事件，执行相关工作流
     * @param botQQ BOT QQ号
     * @param eventType 事件类型
     * @param botEventData BOT事件数据
     */
    public void handleBotEvent(Long botQQ, String eventType, Object botEventData) {
        try {
            log.info("处理BOT事件: botQQ={}, eventType={}", botQQ, eventType);
            
            // 从Redis获取相关工作流
            List<WorkflowInfo> workflows = redisWorkflowService.getWorkflowsByBotEvent(botQQ, eventType);
            
            if (workflows.isEmpty()) {
                log.info("未找到与BOT事件 {}:{} 相关的工作流", botQQ, eventType);
                return;
            }
            
            // 执行每个工作流
            for (WorkflowInfo workflow : workflows) {
                executeWorkflow(workflow, botEventData);
            }
        } catch (Exception e) {
            log.error("处理BOT事件异常", e);
        }
    }

    /**
     * 执行工作流
     * @param workflowInfo 工作流信息
     * @param botEventData BOT事件数据
     */
    private void executeWorkflow(WorkflowInfo workflowInfo, Object botEventData) {
        try {
            log.info("开始执行工作流: {} (ID: {})", workflowInfo.getName(), workflowInfo.getId());
            
            // 验证工作流配置
            if (!workflowUtil.validateWorkflow(workflowInfo)) {
                log.error("工作流配置验证失败: {}", workflowInfo.getId());
                return;
            }
            
            // 构建工作流图
            WorkflowUtil.WorkflowGraph graph = workflowUtil.buildWorkflowGraph(workflowInfo);
            
            // 执行工作流，传入BOT事件数据
            workflowUtil.executeNodesInTopologicalOrder(graph, botEventData);
            
            log.info("工作流执行完成: {}", workflowInfo.getId());
        } catch (Exception e) {
            log.error("执行工作流异常: {}", workflowInfo.getId(), e);
        }
    }
}
