package com.example.demo.ai.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 代码生成请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateRequest {

    /** 用户需求描述（新建/微调时都需要） */
    private String requirements;

    /** 实体类包名 */
    private String entityPackage;

    /** 方法类包名 */
    private String methodPackage;

    /** 关联的已有插件 ID（更新时为非 null，新建为 null） */
    private String pluginId;
}
