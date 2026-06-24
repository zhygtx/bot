package com.example.demo.pojo.entity.metadata;

import lombok.Builder;
import lombok.Data;

/**
 * 返回值平铺字段信息。
 * 每个实例表示返回值对象中的一个可映射字段。
 */
@Data
@Builder
public class ReturnFieldInfo {

    /** 字段名（JSON 名称，优先 @JsonProperty，回退到 Java 字段名） */
    private String name;

    /** 字段类型简名 */
    private String type;

    /** 字段描述 */
    private String description;

    /** 字段完整路径，如 "data.groupId"，"sender.nickname"，用于 DataMap.sourcePath */
    private String fieldPath;

    /** 是否继承自父类 */
    private boolean inherited;

    /** 排序 */
    private int order;
}
