package com.example.demo.manager.event.listener;

import com.example.demo.manager.WorkflowLogManager;
import com.example.demo.manager.event.record.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 工作流日志事件监听器
 * 监听工作流执行过程中的各类事件，驱动日志的创建、更新和持久化
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowLogListener {

    private final WorkflowLogManager logManager;

    /**
     * 工作流开始 —— 创建 WorkflowLog
     */
    @EventListener
    public void onWorkflowStarted(WorkflowStarted event) {
        logManager.createWorkflowLog(
                event.workflowId(),
                event.userId(),
                event.workflowName(),
                event.expectedNodeCount(),
                event.initialContext(),
                event.startTime()
        );
    }

    /**
     * 节点开始执行 —— 创建 NodeLog
     */
    @EventListener
    public void onNodeExecutionStarted(NodeExecutionStarted event) {
        logManager.createNodeLog(
                event.nodeId(),
                event.methodId(),
                event.order(),
                event.methodName(),
                event.methodDescription()
        );
    }

    /**
     * 节点输入输出记录 —— 填充 NodeLog 的 input/output
     */
    @EventListener
    public void onNodeInputOutputRecorded(NodeInputOutputRecorded event) {
        logManager.recordNodeInputOutput(event.nodeId(), event.parameters(), event.result());
    }

    /**
     * 节点执行成功 —— 设置执行耗时
     */
    @EventListener
    public void onNodeExecutionCompleted(NodeExecutionCompleted event) {
        logManager.recordNodeSuccess(event.nodeId(), event.elapsedMs());
    }

    /**
     * 节点执行失败 —— 记录错误信息
     */
    @EventListener
    public void onNodeExecutionFailed(NodeExecutionFailed event) {
        logManager.recordNodeError(event.nodeId(), event.elapsedMs(), event.error());
    }

    /**
     * 节点被跳过 —— 记录调试日志
     */
    @EventListener
    public void onNodeExecutionSkipped(NodeExecutionSkipped event) {
        log.debug("节点 {} 因条件分支被跳过：{}", event.nodeId(), event.reason());
    }

    /**
     * 工作流正常完成 —— 持久化日志
     */
    @EventListener
    public void onWorkflowCompleted(WorkflowCompleted event) {
        logManager.finalizeAndSave(event.startTime());
    }

    /**
     * 工作流执行失败 —— 持久化错误日志
     */
    @EventListener
    public void onWorkflowFailed(WorkflowFailed event) {
        logManager.finalizeAndSaveWithError(event.startTime(), event.error());
    }
}
