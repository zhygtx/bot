package com.example.demo.manager.event.record;

/**
 * 工作流开始执行事件
 */
public record WorkflowStarted(
        String workflowId,
        String userId,
        String workflowName,
        int expectedNodeCount,
        Object initialContext,
        long startTime
) {}
