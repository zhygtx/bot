package com.example.demo.event.record;

/**
 * 节点因条件分支被跳过事件
 */
public record NodeExecutionSkipped(String nodeId, String reason) {}
