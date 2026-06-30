package com.example.demo.ai.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 插件编译上传响应。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginCompileResponse {

    /** 插件 ID，审查或编译失败时为空 */
    private String pluginId;

    /** 版本 ID，审查或编译失败时为空 */
    private String versionId;

    /** 编译前审查结果，审查关闭时为空 */
    private Object reviewResult;
}
