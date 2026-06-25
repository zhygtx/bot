package com.example.demo.manager.event.record;

/**
 * 节点执行失败事件
 */
public record NodeExecutionFailed(String nodeId, long elapsedMs, Throwable error) {}
