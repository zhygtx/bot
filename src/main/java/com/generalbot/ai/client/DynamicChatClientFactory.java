package com.generalbot.ai.client;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.generalbot.ai.mapper.UserAIConfigMapper;
import com.generalbot.ai.tool.PluginFileTool;
import com.generalbot.ai.entity.UserAIConfig;
import com.generalbot.config.DefaultProperties;
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

    /**
     * 获取用户实际使用的插件模型名（用户配置优先，无配置时用默认配置）。
     */
    public String getPluginModel(String userId) {
        return resolveConfig(userId).getModel();
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
        UserAIConfig config = resolveConfig(userId);
        if (config.getApiProvider() == UserAIConfig.ApiProvider.ANTHROPIC) {
            return buildAnthropic(config);
        }
        return buildOpenAi(config);  // 默认 openai 格式（含 DeepSeek 等兼容 API）
    }

    /** 解析用户 AI 配置：优先用户表配置，其次使用默认配置 */
    private UserAIConfig resolveConfig(String userId) {
        UserAIConfig config = userAIConfigMapper.selectOne(new LambdaQueryWrapper<UserAIConfig>()
                .eq(UserAIConfig::getUserId, userId));
        if (config != null) {
            return config;
        }
        return UserAIConfig.builder()
                .baseUrl(defaultProperties.getPlugin().getBaseUrl())
                .apiKey(defaultProperties.getPlugin().getApiKey())
                .model(defaultProperties.getPlugin().getPluginModel())
                .apiProvider(UserAIConfig.ApiProvider.valueOf(defaultProperties.getPlugin().getProvider().toUpperCase()))
                .build();
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
