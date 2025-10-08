package com.news.bot.wf.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.houbb.opencc4j.util.ZhConverterUtil;
import com.news.bot.infrastructure.UrlService;
import com.news.bot.wf.pojo.Fissure;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;


/**
 * FissuresUtil 类用于获取和处理裂隙（Fissures）数据
 * 它依赖于 UrlService 来获取数据，并使用 ObjectMapper 来解析 JSON 数据
 */
@Component
public class FissuresUtil {

    // objectMapper 用于 JSON 数据的序列化与反序列化
    private final ObjectMapper objectMapper = new ObjectMapper();
    // urlService 用于网络请求，获取数据
    private final UrlService urlService;

    /**
     * 构造函数，初始化 FissuresUtil
     * 注册 JavaTimeModule 以支持时间相关的序列化与反序列化
     *
     * @param urlService 用于网络请求的服务接口
     */
    public FissuresUtil(UrlService urlService) {
        this.urlService = urlService;
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * 异步获取裂隙数据
     * 该方法返回一个 CompletableFuture，包含裂隙数据的 JSON 字符串
     *
     * @return CompletableFuture 包含裂隙数据的 JSON 字符串
     */
    public CompletableFuture<String> getFissuresAsync() {
        String url = "https://api.warframestat.us/pc/fissures?language=zh";
        return urlService.fetchJsonAsync(url);
    }

    /**
     * 异步获取并转换裂隙数据
     * 该方法在获取裂隙数据后，将其转换为简体中文
     *
     * @return CompletableFuture 包含转换为简体中文的裂隙数据 JSON 字符串
     */
    public CompletableFuture<String> getAndConvertFissuresAsync() {
        return getFissuresAsync().thenApply(ZhConverterUtil::toSimple);
    }

    /**
     * 解析裂隙数据
     * 该方法将 JSON 字符串解析为 Fissure 对象列表
     *
     * @param json 裂隙数据的 JSON 字符串
     * @return Fissure 对象列表
     * @throws IOException 如果解析过程中发生错误
     */
    public List<Fissure> parseFissures(String json) throws IOException {
        return objectMapper.readValue(json, new TypeReference<>() {
        });
    }

    /**
     * 根据裂隙的过期时间计算剩余时间，并设置为可读的格式
     * 格式示例：
     * - 1h 23m 45s
     * - 23m 45s
     * - 45s
     *
     * @param fissures 裂隙列表
     */
    public void setEta(List<Fissure> fissures) {
        Iterator<Fissure> iterator = fissures.iterator();
        ZonedDateTime now = ZonedDateTime.now();

        while (iterator.hasNext()) {
            Fissure fissure = iterator.next();
            ZonedDateTime expiry = fissure.getExpiry();
            long seconds = Duration.between(now, expiry).getSeconds();

            // 如果已经过期，从列表中移除
            if (seconds < 0) {
                iterator.remove();
                continue;
            }

            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            long secs = seconds % 60;

            StringBuilder etaBuilder = new StringBuilder();
            if (hours > 0) {
                etaBuilder.append(hours).append("时");
            }
            if (minutes > 0) {
                etaBuilder.append(minutes).append("分");
            }
            etaBuilder.append(secs).append("秒");

            fissure.setEta(etaBuilder.toString());
        }
    }

    /**
     * 按 tier 分组，并返回一个 Map，键为 tier，值为 tier 对应的 Fissure 列表
     *
     * @param fissures 裂隙列表
     * @return 按 tier 分组的 Fissure 列表
     */
    public static Map<String, List<Fissure>> groupFissuresByTier(List<Fissure> fissures) {
        if (fissures == null || fissures.isEmpty()) {
            return Collections.emptyMap();
        }

        // 先按 tier_num 排序，保证输出顺序正确
        List<Fissure> sortedList = fissures.stream()
                .sorted(Comparator.comparingInt(Fissure::getTierNum))
                .toList();

        // 按 tier 分组，但仍保持排序后的顺序
        Map<String, List<Fissure>> result = new LinkedHashMap<>();
        for (Fissure fissure : sortedList) {
            result.computeIfAbsent(fissure.getTier(), k -> new ArrayList<>()).add(fissure);
        }

        return result;
    }
}