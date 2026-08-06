package com.example.demo.ai.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.ai.ai.factory.DynamicChatClientFactory;
import com.example.demo.ai.ai.mapper.AIMessageSummaryMapper;
import com.example.demo.ai.ai.pojo.entity.AIChatMessage;
import com.example.demo.ai.ai.pojo.entity.AIMessageSummary;
import com.example.demo.ai.ai.pojo.entity.Code;
import com.example.demo.ai.ai.service.ContextCompressionService;
import com.example.demo.ai.ai.util.AIUtil;
import com.example.demo.ai.ai.util.HttpRequestUtil;
import com.example.demo.config.DefaultProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class ContextCompressionServiceImpl implements ContextCompressionService {

    private static final String MODEL_CATALOG_KEY = "ai:model-catalog";
    private static final double COMPRESS_RATIO = 0.8;
    private static final List<String> CATALOG_URLS = List.of(
            "https://models.dev/api.json",
            "https://raw.githubusercontent.com/anomalyco/models.dev/dev/models.json"
    );
    /** 模型目录兜底文件（classpath 资源），远程全部失败且 Redis 无数据时使用 */
    private static final String FALLBACK_CATALOG_PATH = "classpath:ai/model-catalog-fallback.json";

    private final AIMessageSummaryMapper summaryMapper;
    private final DynamicChatClientFactory chatClientFactory;
    private final AIUtil aiUtil;
    private final DefaultProperties defaultProperties;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final HttpRequestUtil httpRequestUtil;

    /** 构造压缩服务 */
    public ContextCompressionServiceImpl(AIMessageSummaryMapper summaryMapper,
                                         DynamicChatClientFactory chatClientFactory,
                                         AIUtil aiUtil,
                                         DefaultProperties defaultProperties,
                                         @Qualifier("customStringRedisTemplate") StringRedisTemplate stringRedisTemplate,
                                         ObjectMapper objectMapper,
                                         HttpRequestUtil httpRequestUtil) {
        this.summaryMapper = summaryMapper;
        this.chatClientFactory = chatClientFactory;
        this.aiUtil = aiUtil;
        this.defaultProperties = defaultProperties;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.httpRequestUtil = httpRequestUtil;
    }

    // ==================== 摘要持久化 ====================

    /** 查询会话中 summary_round 最大的摘要；无摘要时返回 null */
    @Override
    public AIMessageSummary findLatestSummary(String conversationId) {
        if (conversationId == null) {
            return null;
        }
        return summaryMapper.selectList(new LambdaQueryWrapper<AIMessageSummary>()
                        .eq(AIMessageSummary::getConversationId, conversationId)
                        .orderByDesc(AIMessageSummary::getSummaryRound))
                .stream().findFirst().orElse(null);
    }

    /** 新增一条摘要：补全主键与创建时间后写入 */
    @Override
    public void insertSummary(AIMessageSummary summary) {
        summary.setId(UUID.randomUUID().toString());
        summary.setCreateTime(LocalDateTime.now());
        summaryMapper.insert(summary);
    }

    // ==================== 模型上下文目录 ====================

    /** 应用启动后异步拉取一次模型目录，避免阻塞启动 */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        CompletableFuture.runAsync(this::refreshModelCatalog);
    }

    /** 每天凌晨 4 点刷新模型目录；全部失败时保留 Redis 旧数据，Redis 无数据时使用兜底文件 */
    @Scheduled(cron = "0 0 4 * * ?")
    public synchronized void refreshModelCatalog() {
        for (String url : CATALOG_URLS) {
            String body = httpRequestUtil.getText(url);
            if (body == null || body.isBlank()) {
                continue;
            }
            try {
                Map<String, ModelProfile> profiles = normalizeProfiles(objectMapper.readTree(body));
                if (profiles.isEmpty()) {
                    continue;
                }
                writeCatalog(profiles);
                log.info("模型上下文目录已刷新: {} 个模型, source={}", profiles.size(), url);
                return;
            } catch (Exception e) {
                log.warn("模型目录解析/写入失败: {} - {}", url, e.getMessage());
            }
        }
        if (stringRedisTemplate.hasKey(MODEL_CATALOG_KEY)) {
            log.warn("模型目录拉取失败，继续使用 Redis 旧数据");
            return;
        }
        try {
            String fallback = aiUtil.loadTemplate(FALLBACK_CATALOG_PATH);
            Map<String, ModelProfile> profiles = objectMapper.readValue(fallback, new TypeReference<>() {});
            if (!profiles.isEmpty()) {
                writeCatalog(profiles);
                log.info("模型目录拉取失败，使用兜底文件: {} 个模型", profiles.size());
            }
        } catch (Exception e) {
            log.warn("加载模型目录兜底文件失败: {}", e.getMessage());
        }
    }

    /** 把规范化后的模型档案写入 Redis */
    private void writeCatalog(Map<String, ModelProfile> profiles) throws JsonProcessingException {
        stringRedisTemplate.opsForValue().set(MODEL_CATALOG_KEY, objectMapper.writeValueAsString(profiles));
    }

    /** 把 models.dev 的 {data:[...]} 规范化为 {modelId -> ModelProfile}，并按 id 与 / 后缀建立索引 */
    private Map<String, ModelProfile> normalizeProfiles(JsonNode root) {
        Map<String, ModelProfile> profiles = new HashMap<>();
        JsonNode data = root.path("data");
        if (!data.isArray()) {
            return profiles;
        }
        for (JsonNode node : data) {
            int contextWindow = node.path("context_length").asInt(0);
            if (contextWindow <= 0) {
                continue;
            }
            String id = node.path("id").asText("");
            if (id.isBlank()) {
                continue;
            }
            int maxOutput = node.path("top_provider").path("max_completion_tokens").asInt(0);
            ModelProfile profile = new ModelProfile(contextWindow, maxOutput);
            profiles.putIfAbsent(id, profile);
            int idx = id.lastIndexOf('/');
            if (idx >= 0 && idx < id.length() - 1) {
                profiles.putIfAbsent(id.substring(idx + 1), profile);
            }
        }
        return profiles;
    }

    /** 从 Redis 目录中查找上下文窗口：先精确匹配 ID，再去掉 provider 前缀匹配，最后模糊匹配；查不到返回 null */
    private Integer findContextWindow(String modelId) {
        if (modelId == null || modelId.isBlank()) {
            return null;
        }
        String json = stringRedisTemplate.opsForValue().get(MODEL_CATALOG_KEY);
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            Map<String, ModelProfile> profiles = objectMapper.readValue(json, new TypeReference<>() {});
            ModelProfile profile = profiles.get(modelId);
            if (profile == null) {
                int idx = modelId.lastIndexOf('/');
                if (idx >= 0 && idx < modelId.length() - 1) {
                    profile = profiles.get(modelId.substring(idx + 1));
                }
            }
            if (profile == null) {
                profile = fuzzyFindProfile(profiles, modelId);
            }
            return profile == null ? null : profile.getContextWindow();
        } catch (Exception e) {
            log.warn("解析模型上下文目录失败: {}", e.getMessage());
            return null;
        }
    }

    /** 模糊匹配：先忽略大小写精确匹配，再匹配"目录后缀以用户模型名开头"的版本化模型 */
    private ModelProfile fuzzyFindProfile(Map<String, ModelProfile> profiles, String modelId) {
        String suffix = modelId.contains("/") ? modelId.substring(modelId.lastIndexOf('/') + 1) : modelId;
        if (suffix.isBlank()) {
            return null;
        }
        String lower = suffix.toLowerCase(Locale.ROOT);

        // 忽略大小写精确匹配
        ModelProfile hit = profiles.entrySet().stream()
                .filter(entry -> entry.getKey().toLowerCase(Locale.ROOT).equals(lower))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
        if (hit != null) {
            return hit;
        }

        // 目录中的裸模型名以用户模型名为开头，且分隔符后才是版本号（例如 gpt-4o -> gpt-4o-2024-08-06）
        return profiles.entrySet().stream()
                .filter(entry -> !entry.getKey().contains("/"))
                .filter(entry -> {
                    String key = entry.getKey().toLowerCase(Locale.ROOT);
                    return key.startsWith(lower)
                            && (key.length() == lower.length()
                            || "-._:".indexOf(key.charAt(lower.length())) >= 0);
                })
                .sorted(Comparator.comparingInt(entry -> entry.getKey().length()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    // ==================== 压缩与 prompt 拼装 ====================

    /** 组装 Spring AI 消息：需要时先压缩历史，再按"最新摘要 + 尾部轮次 + 当前需求"拼接；压缩失败回退全量历史 */
    @Override
    public List<Message> buildMessages(String userId, String conversationId, List<AIChatMessage> history,
                                       AIChatMessage newMessage, List<Code> codes, String systemTemplate) {
        try {
            // 达到上下文窗口 80% 时用用户模型压缩历史并新增摘要（单次调用逻辑直接内联）
            if (history != null && !history.isEmpty() && conversationId != null) {
                AIChatMessage last = history.get(history.size() - 1);
                Integer contextWindow = last.getPromptTokens() == null ? null
                        : findContextWindow(chatClientFactory.getPluginModel(userId));
                if (contextWindow != null && contextWindow > 0
                        && last.getPromptTokens() >= (int) (contextWindow * COMPRESS_RATIO)) {
                    AIMessageSummary previous = findLatestSummary(conversationId);
                    String input = aiUtil.buildCompressionInput(history, previous);
                    if (input != null && !input.isBlank()) {
                        String template = aiUtil.loadTemplate(defaultProperties.getCompress().getPromptTemplatePath());
                        String content = chatClientFactory.getPluginChatClient(userId)
                                .prompt(new Prompt(List.of(new SystemMessage(template), new UserMessage(input))))
                                .call()
                                .content();
                        JsonNode parsed = aiUtil.parseJson(content);
                        insertSummary(AIMessageSummary.builder()
                                .conversationId(conversationId)
                                .userId(userId)
                                .summaryRound(last.getRound())
                                .messageId(last.getId())
                                .summaryContent(parsed.toString())
                                .build());
                        log.info("会话 {} 上下文已压缩至第 {} 轮", conversationId, last.getRound());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("上下文压缩失败，回退全量历史: {}", e.getMessage());
        }
        return aiUtil.buildMessages(systemTemplate, findLatestSummary(conversationId), history, newMessage, codes);
    }

    @Data
    public static class ModelProfile {
        private int contextWindow;
        private int maxOutputTokens;

        /** 构造模型档案 */
        public ModelProfile(int contextWindow, int maxOutputTokens) {
            this.contextWindow = contextWindow;
            this.maxOutputTokens = maxOutputTokens;
        }
    }
}
