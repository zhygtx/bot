package com.example.demo.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "ai.default")
public class DefaultProperties {

    private Plugin plugin = new Plugin();
    private Review review = new Review();

    @PostConstruct
    public void validate() {
        // plugin 始终必填
        Assert.hasText(plugin.provider, "ai.default.plugin.provider不得为空");
        Assert.hasText(plugin.baseUrl, "ai.default.plugin.base-url不得为空");
        Assert.hasText(plugin.apiKey, "ai.default.plugin.api-key不得为空");
        Assert.hasText(plugin.pluginModel, "ai.default.plugin.plugin-model不得为空");
        Assert.hasText(plugin.promptTemplatePath, "ai.default.plugin.prompt-template-path不得为空");

        // review 仅在 enabled=true 时要求填写
        if (Boolean.TRUE.equals(review.enabled)) {
            Assert.hasText(review.provider, "ai.default.review.provider当启用编译前审查时不得为空");
            Assert.hasText(review.baseUrl, "ai.default.review.base-url当启用编译前审查时不得为空");
            Assert.hasText(review.apiKey, "ai.default.review.api-key当启用编译前审查时不得为空");
            Assert.hasText(review.reviewModel, "ai.default.review.review-model当启用编译前审查时不得为空");
            Assert.hasText(review.promptTemplatePath, "ai.default.review.prompt-template-path当启用编译前审查时不得为空");
        }
    }

    @Data
    public static class Plugin {
        private String provider;
        private String baseUrl;
        private String apiKey;
        private String pluginModel;
        private String promptTemplatePath;
    }

    @Data
    public static class Review {
        private Boolean enabled = false;
        private String provider;
        private String baseUrl;
        private String apiKey;
        private String reviewModel;
        private String promptTemplatePath;
    }
}