package com.example.demo.manager.event.record;

/**
 * 节点开始执行事件
 */
public record NodeExecutionStarted(
        String nodeId,
        String methodId,
        int order,
        String methodName,
        String methodDescription
) {}
