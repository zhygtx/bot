package com.generalbot.workflow.entity.execution;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 一次工作流执行的完整节点轨迹。
 */
@Data
public class ExecutionTrace {

    /**
     * 节点执行记录
     */
    private List<NodeTrace> nodes = new ArrayList<>();
}
