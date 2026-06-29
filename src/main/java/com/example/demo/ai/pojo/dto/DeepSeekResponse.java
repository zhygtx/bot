package com.example.demo.ai.pojo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DeepSeek Chat API 响应体
 */
@Data
@NoArgsConstructor
public class DeepSeekResponse {
    private String id;
    private String object;
    private Long created;
    private String model;
    private List<Choice> choices;
    private Usage usage;
    private ErrorDetail error;

    @Data
    @NoArgsConstructor
    public static class Choice {
        private Integer index;
        private Message message;
        @JsonProperty("finish_reason")
        private String finishReason;
    }

    @Data
    @NoArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }

    @Data
    @NoArgsConstructor
    public static class Usage {
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;
        @JsonProperty("completion_tokens")
        private Integer completionTokens;
        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }

    @Data
    @NoArgsConstructor
    public static class ErrorDetail {
        private String message;
        private String type;
        private String code;
    }
}
