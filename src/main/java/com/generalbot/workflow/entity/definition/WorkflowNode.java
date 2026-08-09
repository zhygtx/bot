package com.generalbot.workflow.entity.definition;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作流节点。callable 是统一的函数引用，系统节点和插件节点共用。
 */
@Data
public class WorkflowNode {

    /**
     * 节点ID，前端生成并保持稳定
     */
    private String id;

    /**
     * 水平位置
     */
    private Integer x;

    /**
     * 垂直位置
     */
    private Integer y;

    /**
     * callable 引用，例如 system:botEvent:message:group:normal、plugin:{pluginId}:{versionId}:{methodId}
     */
    private String callable;

    /**
     * 参数输入配置
     */
    private List<ParamInput> inputs = new ArrayList<>();

    /**
     * 是否分支节点（返回 Boolean，具有 success/failure 两个输出口）
     */
    private Boolean branch = false;

    /**
     * 触发节点专用配置，例如 botQQ、cronExpression
     */
    private Map<String, Object> config = new HashMap<>();
}
