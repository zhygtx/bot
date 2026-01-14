package com.example.demo.pojo.task.actionContent;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("id")
    private String id;

    /**
     * 所属用户ID
     */
    @JsonProperty("userId")
    private String userId;

    /**
     * 模板名称
     */
    @JsonProperty("name")
    private String name;

    /**
     * 模板内容
     */
    @JsonProperty("content")
    private String content;

    /**
     * 模板宽度
     */
    @JsonProperty("width")
    private Integer width;

    /**
     * 模板高度
     */
    @JsonProperty("height")
    private Integer height;

    /**
     * 模板数据ID
     */
    @JsonProperty("dataId")
    private String dataId;

    /**
     * 模板类型
     */
    @JsonProperty("templateType")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private TemplateType templateType;

    /**
     * 模板类型枚举
     */
    public enum TemplateType {
        text,api,url
    }
}
