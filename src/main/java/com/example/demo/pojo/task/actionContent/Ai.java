package com.example.demo.pojo.task.actionContent;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Ai {

    /**
     * AI UUID
     */
    @JsonProperty("id")
    private String id;

    /**
     * 所属用户ID
     */
    @JsonProperty("userId")
    private String userId;

    /**
     * AI名称
     */
    @JsonProperty("name")
    private String name;

    /**
     * AI设定（如身份、性格、说话语气之类的）
     */
    @JsonProperty("setting")
    private String setting;

    /**
     * AI API Key
     */
    @JsonProperty("apiKey")
    private String apiKey;

    /**
     * 压缩阈值0~1
     */
    @JsonProperty("compressPct")
    private Double compressPct;

    /**
     * AI模型
     */
    @JsonProperty("model")
    private Model model;

    /**
     * 模型枚举类
     */
    @Getter
    public enum Model {
        @JsonProperty("qwen3-vl-plus-2025-12-19")
        qwen3_vl_plus_2025_12_19("qwen3-vl-plus-2025-12-19"),
        qwen3_vl_flash("qwen3-vl-flash"),
        qwen_flash("qwen-flash");

        private final String modelName;

        Model(String modelName) {
            this.modelName = modelName;
        }

        private static final Map<Model, Long> MODEL_MAP = new HashMap<>();

        static {
            MODEL_MAP.put(qwen3_vl_plus_2025_12_19, 32000L);
            MODEL_MAP.put(qwen3_vl_flash, 32000L);
            MODEL_MAP.put(qwen_flash, 128000L);
        }

        public static Long getMaxToken(Model modelName) {
            return MODEL_MAP.get(modelName);
        }
    }
}
