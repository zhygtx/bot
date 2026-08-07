package com.generalbot.workflow.entity.definition;

import lombok.Data;

/**
 * 工作流连线。port 为 success/failure，普通节点只有 success。
 */
@Data
public class WorkflowEdge {

    /**
     * 来源节点ID
     */
    private String from;

    /**
     * 目标节点ID
     */
    private String to;

    /**
     * 端口：success/failure
     */
    private String port = "success";
}
