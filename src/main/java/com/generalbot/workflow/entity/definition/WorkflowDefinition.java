package com.generalbot.workflow.entity.definition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 工作流定义：整棵 DAG 以 JSON 保存。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowDefinition {

    /**
     * 画布视图状态
     */
    private CanvasView view;

    /**
     * 节点列表
     */
    private List<WorkflowNode> nodes = new ArrayList<>();

    /**
     * 连线列表
     */
    private List<WorkflowEdge> edges = new ArrayList<>();
}
