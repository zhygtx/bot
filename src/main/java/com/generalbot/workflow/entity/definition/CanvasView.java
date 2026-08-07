package com.generalbot.workflow.entity.definition;

import lombok.Data;

/**
 * 画布视图状态。
 */
@Data
public class CanvasView {

    /**
     * 水平偏移
     */
    private Double offsetX;

    /**
     * 垂直偏移
     */
    private Double offsetY;

    /**
     * 缩放比例
     */
    private Double scale;
}
