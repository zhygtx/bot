package com.example.demo.pojo.entity.metadata;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 事件元数据
 */
@Data
@Builder
public class EventMetadata {
    
    /**
     * 事件类型标识
     */
    private String eventType;
    
    /**
     * 事件显示名称
     */
    private String eventName;
    
    /**
     * 事件描述
     */
    private String description;
    
    /**
     * 排序权重
     */
    private int order;
    
    /**
     * 实体类信息
     */
    private EntityMetadata entityInfo;
    
    /**
     * 实体类元数据
     */
    @Data
    @Builder
    public static class EntityMetadata {
        /**
         * 实体类名称
         */
        private String entityName;
        
        /**
         * 字段列表
         */
        private List<FieldMetadata> fields;
    }
}
