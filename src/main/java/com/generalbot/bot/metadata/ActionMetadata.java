package com.generalbot.bot.metadata;

import com.generalbot.plugin.entity.ParameterInfo;
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

    /** 分类名称列表 */
    private List<String> categories;

    /** 分类排序列表 */
    private List<Integer> categoryOrders;

    /** 返回值元数据 */
    private ActionReturnInfo returnInfo;
}
