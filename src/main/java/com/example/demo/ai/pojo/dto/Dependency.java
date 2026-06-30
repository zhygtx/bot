package com.example.demo.ai.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 声明的 Maven 依赖
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dependency {
    private String groupId;
    private String artifactId;
    private String version;
    @Builder.Default
    private String scope = "compile";
}
