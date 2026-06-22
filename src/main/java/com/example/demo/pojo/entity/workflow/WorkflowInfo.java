package com.example.demo.pojo.entity.workflow;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
     * 工作流创建者名称
     */
    private String authorName;

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
     * 工作流节点列表
     */
    private List<Node> nodes;

    /**
     * 工作流画布视图状态
     */
    private WorkflowCanvasView workflowCanvasView;
}
