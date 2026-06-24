package com.example.demo.pojo.entity.plugin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 复杂参数平铺子字段信息。
 * 当方法参数类型为复杂 POJO 时，将字段平铺展示给前端用户做数据映射。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParameterFieldInfo {

    /** 字段名（JSON 名称，优先 @JsonProperty，回退到 Java 字段名） */
    private String name;

    /** 字段类型简名 */
    private String type;

    /** 字段描述 */
    private String description;

    /** 字段路径，用于 DataMap.targetPath 或 DataMap.targetParamName */
    private String fieldPath;

    /** 是否必须 */
    private boolean required;

    /** 排序 */
    private int order;
}
