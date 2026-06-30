package com.example.demo.ai.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 流式生成请求，兼容首次生成与后续微调。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StreamGenerateRequest {
    private String conversationId;
    private String requirements;
    private String instruction;
    private String entityPackage;
    private String methodPackage;
    private String pluginId;
}
