package com.example.demo.manager.event.record;

/**
 * 工作流执行失败事件
 */
public record WorkflowFailed(long startTime, Throwable error) {}
