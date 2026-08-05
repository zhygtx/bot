package com.example.demo.ai.ai.factory;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.ai.ai.mapper.UserAIConfigMapper;
import com.example.demo.ai.ai.tool.PluginFileTool;
import com.example.demo.ai.ai.pojo.entity.UserAIConfig;
import com.example.demo.config.DefaultProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class DynamicChatClientFactory {

    private final UserAIConfigMapper userAIConfigMapper;
    private final DefaultProperties defaultProperties;
    private final PluginFileTool pluginFileTool;
    private final Map<String, ChatClient> cache = new ConcurrentHashMap<>();

    public DynamicChatClientFactory(DefaultProperties defaultProperties, UserAIConfigMapper userAIConfigMapper,
                                     @org.springframework.context.annotation.Lazy PluginFileTool pluginFileTool) {
        this.defaultProperties = defaultProperties;
        this.userAIConfigMapper = userAIConfigMapper;
        this.pluginFileTool = pluginFileTool;
    }

    /**
     * 获取用户的插件生成 ChatClient（带缓存）
     */
    public ChatClient getPluginChatClient(String userId) {
        // 如果配置没变，复用缓存
        return cache.computeIfAbsent(userId, k -> buildClient(userId));
    }

    public ChatClient getReviewChatClient() {
        var reviewProps = defaultProperties.getReview();
        if (!reviewProps.getEnabled()) {
            return null;
        }
        return cache.computeIfAbsent("review", k -> {
            var provider = reviewProps.getProvider();
            UserAIConfig config = UserAIConfig.builder()
                    .baseUrl(reviewProps.getBaseUrl())
                    .apiKey(reviewProps.getApiKey())
                    .model(reviewProps.getReviewModel())
                    .apiProvider(UserAIConfig.ApiProvider.valueOf(provider.toUpperCase()))
                    .build();
            return provider.equalsIgnoreCase("ANTHROPIC")
                    ? buildAnthropic(config)
                    : buildOpenAi(config);
        });
    }

    /** 用户更新配置后清除缓存 */
    public void evictCache(String userId) {
        if (!cache.containsKey(userId)) {
            return;
        }
        cache.remove(userId);
    }

    private ChatClient buildClient(String userId) {
        UserAIConfig config = userAIConfigMapper.selectOne(new LambdaQueryWrapper<UserAIConfig>()
                        .eq(UserAIConfig::getUserId, userId));

        if (config == null) {
            config = UserAIConfig.builder()
                    .baseUrl(defaultProperties.getPlugin().getBaseUrl())
                    .apiKey(defaultProperties.getPlugin().getApiKey())
                    .model(defaultProperties.getPlugin().getPluginModel())
                    .apiProvider(UserAIConfig.ApiProvider.valueOf(defaultProperties.getPlugin().getProvider().toUpperCase()))
                    .build();
        }
        if (config.getApiProvider() == UserAIConfig.ApiProvider.ANTHROPIC) {
            return buildAnthropic(config);
        }
        return buildOpenAi(config);  // 默认 openai 格式（含 DeepSeek 等兼容 API）
    }

    private ChatClient buildOpenAi(UserAIConfig config) {
        OpenAiApi api = OpenAiApi.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .build();
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(OpenAiChatOptions.builder().model(config.getModel()).build())
                .build();
        return ChatClient.builder(chatModel)
                .defaultTools(pluginFileTool)
                .build();
    }

    private ChatClient buildAnthropic(UserAIConfig config) {
        AnthropicApi api = AnthropicApi.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .build();
        AnthropicChatModel chatModel = AnthropicChatModel.builder()
                .anthropicApi(api)
                .defaultOptions(AnthropicChatOptions.builder().model(config.getModel()).build())
                .build();
        return ChatClient.builder(chatModel)
                .defaultTools(pluginFileTool)
                .build();
    }
}