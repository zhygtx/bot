package com.generalbot.workflow.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import com.generalbot.workflow.entity.definition.WorkflowDefinition;

/**
 * 工作流信息。定义以 JSON 形式整体保存在 definition 字段中。
 */
@Data
public class WorkflowInfo {

    /**
     * 工作流ID
     */
    private String id;

    /**
     * 工作流创建者ID
     */
    private String userId;

    /**
     * 工作流名称
     */
    private String name;

    /**
     * 工作流是否启用
     */
    private Boolean enabled;

    /**
     * 工作流是否可用
     */
    private Boolean available;

    /**
     * 禁用原因
     */
    private String disableReason;

    /**
     * 触发键，例如 botEvent:{botQQ}:{EventType} 或 schedule:{cron}
     */
    private String triggerKey;

    /**
     * 工作流定义（节点、连线、画布视图）
     */
    private WorkflowDefinition definition;

    /**
     * 工作流创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 工作流更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
