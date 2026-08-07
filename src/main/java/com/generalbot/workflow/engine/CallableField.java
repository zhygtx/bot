package com.generalbot.workflow.engine;

import lombok.Builder;
import lombok.Data;

/**
 * callable 返回值的可映射字段描述。
 * path 为空表示整个返回值，path 非空表示返回值对象上的嵌套字段路径。
 */
@Data
@Builder
public class CallableField {

    /**
     * 字段名（前端展示）
     */
    private String name;

    /**
     * 字段类型
     */
    private String type;

    /**
     * 字段描述
     */
    private String description;

    /**
     * 字段路径；空串表示整个返回值
     */
    private String path;
}
