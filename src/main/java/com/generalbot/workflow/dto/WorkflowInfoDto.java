package com.generalbot.workflow.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 工作流信息DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowInfoDto {

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
     * 触发键
     */
    private String triggerKey;

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

    /**
     * 工作流执行次数
     */
    private Integer executeCount;

    /**
     * 工作流平均执行时间
     */
    private Integer averageExecutionTime;

    /**
     * 工作流节点数
     */
    private Integer nodeCount;

    /**
     * 工作流平均节点数
     */
    private Integer averageNodeCount;
}
