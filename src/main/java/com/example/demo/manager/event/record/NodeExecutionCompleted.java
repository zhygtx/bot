package com.example.demo.manager.event.record;

/**
 * 节点执行成功完成事件
 */
public record NodeExecutionCompleted(String nodeId, long elapsedMs) {}
