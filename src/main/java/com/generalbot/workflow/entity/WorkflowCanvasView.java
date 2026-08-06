package com.generalbot.workflow.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工作流画布视图状态
 * 用于保存用户编辑时的画布尺寸与当前视图位置/缩放
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowCanvasView {

    /**
     * 工作流ID
     */
    private String workflowId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 视图横向偏移（px）
     */
    private Double offsetX;

    /**
     * 视图纵向偏移（px）
     */
    private Double offsetY;

    /**
     * 缩放比例（0.3 ~ 2.0）
     */
    private Double scale;
}