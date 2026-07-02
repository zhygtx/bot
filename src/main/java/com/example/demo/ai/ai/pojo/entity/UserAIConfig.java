package com.example.demo.ai.ai.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("ai_config")
public class AIConfig {

    /** 主键ID */
    private String id;

    /** 用户ID */
    private String userId;

    /** API接口类型*/
    private ApiProvider apiProvider;

    /** API接口基础 URL */
    private String baseUrl;

    /** API密钥 */
    private String apiKey;

    /** 模型名称 */
    private String model;

    /** API接口类型枚举 */
    public enum ApiProvider {
        /** OpenAI */
        OpenAI,
        /** Anthropic */
        Anthropic
    }
}