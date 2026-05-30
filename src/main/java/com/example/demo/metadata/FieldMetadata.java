package com.example.demo.metadata;

import lombok.Builder;
import lombok.Data;

/**
 * 字段元数据
 */
@Data
@Builder
public class FieldMetadata {
    
    /**
     * 字段名称
     */
    private String fieldName;
    
    /**
     * 字段类型
     */
    private String fieldType;
    
    /**
     * 字段描述
     */
    private String description;
    
    /**
     * 排序权重
     */
    private int order;
    
    /**
     * 是否为继承字段
     */
    private boolean inherited;
}
