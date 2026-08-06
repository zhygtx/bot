package com.generalbot.workflow.entity.log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BigText {
    
    private String key;
    private String value;

    /**
     * 关联的工作流日志 ID，用于 workflow_log 删除时级联清理 big_text
     */
    private Long workflowLogId;
}
