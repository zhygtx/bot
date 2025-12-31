package com.example.demo.pojo.task.actionContent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模板实体类
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Template {

    /**
     * 模板ID
     */
    private String id;

    /**
     * 所属用户ID
     */
    private String userId;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 模板内容
     */
    private String content;

    /**
     * 模板宽度
     */
    private Integer width;

    /**
     * 模板高度
     */
    private Integer height;

    /**
     * 模板数据ID
     */
    private String dataId;

    /**
     * 模板类型
     */
    private TemplateType templateType;

    /**
     * 模板类型枚举
     */
    public enum TemplateType {
        text,api,url
    }
}
