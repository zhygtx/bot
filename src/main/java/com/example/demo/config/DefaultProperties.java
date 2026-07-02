package com.example.demo.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "ai.default")
public class AiDefaultProperties {

    private Plugin plugin = new Plugin();
    private Review review = new Review();

    @PostConstruct
    public void validate() {
        // plugin 始终必填
        Assert.hasText(plugin.provider, "ai.default.plugin.provider is required");
        Assert.hasText(plugin.baseUrl, "ai.default.plugin.base-url is required");
        Assert.hasText(plugin.apiKey, "ai.default.plugin.api-key is required");
        Assert.hasText(plugin.pluginModel, "ai.default.plugin.plugin-model is required");

        // review 仅在 enabled=true 时要求填写
        if (Boolean.TRUE.equals(review.enabled)) {
            Assert.hasText(review.provider, "ai.default.review.provider is required when review is enabled");
            Assert.hasText(review.baseUrl, "ai.default.review.base-url is required when review is enabled");
            Assert.hasText(review.apiKey, "ai.default.review.api-key is required when review is enabled");
            Assert.hasText(review.reviewModel, "ai.default.review.review-model is required when review is enabled");
        }
    }

    @Data
    public static class Plugin {
        private String provider;
        private String baseUrl;
        private String apiKey;
        private String pluginModel;
    }

    @Data
    public static class Review {
        private Boolean enabled;
        private String provider;
        private String baseUrl;
        private String apiKey;
        private String reviewModel;
    }
}