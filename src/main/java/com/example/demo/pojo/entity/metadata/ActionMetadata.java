package com.example.demo.pojo.entity.metadata;

import com.example.demo.pojo.entity.plugin.ParameterInfo;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 动作元数据
 */
@Data
@Builder
public class ActionMetadata {
    
    /**
     * 动作方法名
     */
    private String actionName;
    
    /**
     * 动作显示名称
     */
    private String actionDisplayName;
    
    /**
     * 动作描述
     */
    private String description;
    
    /**
     * 排序权重
     */
    private int order;
    
    /**
     * 参数列表
     */
    private List<ParameterInfo> parameters;
}
