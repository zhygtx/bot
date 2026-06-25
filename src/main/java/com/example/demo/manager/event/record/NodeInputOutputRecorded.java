package com.example.demo.event.record;

import java.util.Map;

/**
 * 节点输入输出记录事件（由 recordMethodParameters 触发）
 */
public record NodeInputOutputRecorded(
        String nodeId,
        Map<String, Object> parameters,
        Object result
) {}
