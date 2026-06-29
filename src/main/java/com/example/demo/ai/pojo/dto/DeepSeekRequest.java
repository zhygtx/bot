package com.example.demo.ai.pojo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DeepSeek Chat API 请求体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeepSeekRequest {
    private String model;
    private List<Message> messages;
    private Double temperature;
    @JsonProperty("max_tokens")
    private Integer maxTokens;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }
}
