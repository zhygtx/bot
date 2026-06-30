package com.example.demo.ai.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI 代码生成响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateResponse {

    /** 会话 ID（首次创建时生成，微调/撤销时复用） */
    private String conversationId;

    /** 当前轮次 */
    private Integer round;

    /** AI 生成的代码文件列表 */
    private List<SourceFile> files;

    /** AI 声明的额外 Maven 依赖 */
    private List<Dependency> dependencies;

    /** AI 返回的原始文本（格式异常时用于调试） */
    private String rawResponse;

    /** 代码审查结果（审查关闭时为 null） */
    private Object reviewResult;
}
