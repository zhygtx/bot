package com.generalbot.workflow.entity.execution;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 大数据记录：存放超过内嵌阈值的日志/堆栈内容，key 为引用键。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BigText {

    /**
     * 大数据引用键
     */
    private String key;

    /**
     * 所属工作流执行记录ID，用于级联删除
     */
    private Long executionId;

    /**
     * 实际数据内容
     */
    private String value;
}
