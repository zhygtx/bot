package com.example.demo.ai.util;

import com.example.demo.ai.pojo.dto.DeepSeekRequest;
import com.example.demo.ai.pojo.dto.DeepSeekResponse;
import com.example.demo.ai.pojo.dto.SourceFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI 代码生成器。<br>
 * 职责：加载提示词模板 → 构造完整提示词 → 调用 DeepSeek API → 解析返回的代码文件。
 */
@Slf4j
@Component
public class CodeGenerator {

    // ==================== 配置字段 ====================

    @Value("${ai.plugin.deepseek.api-key}")
    private String apiKey;

    @Value("${ai.plugin.deepseek.base-url}")
    private String baseUrl;

    @Value("${ai.plugin.deepseek.model}")
    private String model;

    @Value("${ai.plugin.deepseek.temperature}")
    private double temperature;

    @Value("${ai.plugin.deepseek.max-tokens}")
    private int maxTokens;

    /** 复用 RestTemplate 实例，避免每次调用重新创建 */
    private final RestTemplate restTemplate;

    public CodeGenerator() {
        this.restTemplate = new RestTemplate();
    }

    // ==================== 内部类型 ====================

    /**
     * 代码生成结果
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GenerateResult {
        /** 是否生成成功 */
        private boolean success;
        /** 解析出的源码文件列表 */
        private List<SourceFile> files;
        /** AI 返回的原始文本（用于调试或格式异常时回退） */
        private String rawResponse;
        /** 错误信息 */
        private String errorMessage;
    }

    // ==================== 公共方法 ====================

    /**
     * 首次生成代码。
     *
     * @param requirements   用户需求描述
     * @param entityPackage  实体类包名
     * @param methodPackage  方法类包名
     * @return 生成结果
     */
    public GenerateResult generateCode(String requirements,
                                       String entityPackage,
                                       String methodPackage) {
        return generateCode(requirements, entityPackage, methodPackage, null);
    }

    /**
     * 基于已有代码微调/更新。
     *
     * @param requirements     用户需求描述
     * @param entityPackage    实体类包名
     * @param methodPackage    方法类包名
     * @param existingCodeJson 已发布的代码 JSON 字符串（可为 null，表示首次生成）
     * @return 生成结果
     */
    public GenerateResult generateCode(String requirements,
                                       String entityPackage,
                                       String methodPackage,
                                       String existingCodeJson) {
        // ========== 校验 API Key ==========
        if (apiKey == null || apiKey.isBlank()) {
            return GenerateResult.builder()
                    .success(false)
                    .errorMessage("DeepSeek API Key 未配置，请设置环境变量 DEEPSEEK_API_KEY")
                    .build();
        }

        try {
            // ========== 构造提示词 ==========
            String prompt = buildPrompt(requirements, entityPackage, methodPackage, existingCodeJson);
            log.debug("构造的提示词（前 200 字符）: {}...", prompt.substring(0, Math.min(prompt.length(), 200)));

            // ========== 调用 DeepSeek API ==========
            String responseContent = callDeepSeek(prompt);

            // ========== 解析返回结果 ==========
            return parseResponse(responseContent);

        } catch (Exception e) {
            log.error("AI 代码生成异常", e);
            return GenerateResult.builder()
                    .success(false)
                    .errorMessage("AI 代码生成失败: " + e.getMessage())
                    .build();
        }
    }

    // ==================== 提示词构造 ====================

    /**
     * 加载提示词模板并替换占位符
     */
    private String buildPrompt(String requirements,
                               String entityPackage,
                               String methodPackage,
                               String existingCodeJson) throws IOException {
        // 加载模板
        String template = loadTemplate();

        // 替换基本占位符
        String prompt = template
                .replace("{{entityPackage}}", entityPackage)
                .replace("{{methodPackage}}", methodPackage)
                .replace("{{requirements}}", requirements);

        // 处理现有代码块
        if (existingCodeJson != null && !existingCodeJson.isBlank()) {
            // 有现有代码：保留块内容，替换标记和内部占位符
            prompt = prompt.replace("{{#existingCode}}", "")
                    .replace("{{/existingCode}}", "")
                    .replace("{{existingCode}}", existingCodeJson);
        } else {
            // 无现有代码：移除整个条件块（包括标记和内部内容）
            prompt = prompt.replaceAll("\\{\\{#existingCode\\}\\}[\\s\\S]*?\\{\\{/existingCode\\}\\}", "");
        }

        return prompt;
    }

    /**
     * 从 classpath 加载提示词模板
     */
    private String loadTemplate() throws IOException {
        ClassPathResource resource = new ClassPathResource("ai-plugins/prompt-template.txt");
        if (!resource.exists()) {
            throw new IOException("找不到提示词模板文件: classpath:ai-plugins/prompt-template.txt");
        }
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    // ==================== DeepSeek API 调用 ====================

    /**
     * 调用 DeepSeek Chat API
     *
     * @param prompt 完整提示词
     * @return AI 返回的文本内容
     */
    private String callDeepSeek(String prompt) {
        // 构造请求
        DeepSeekRequest request = new DeepSeekRequest();
        request.setModel(model);
        request.setTemperature(temperature);
        request.setMaxTokens(maxTokens);
        request.setMessages(List.of(
                new DeepSeekRequest.Message("system",
                        "你是一个 Java 插件代码生成器，严格遵循用户给出的 SDK 规范生成高质量的 Java 代码。"),
                new DeepSeekRequest.Message("user", prompt)
        ));

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<DeepSeekRequest> entity = new HttpEntity<>(request, headers);

        // 构造 URL
        String url = baseUrl.replaceAll("/+$", "") + "/chat/completions";
        log.info("调用 DeepSeek API: url={}, model={}", url, model);

        // 发送请求
        ResponseEntity<DeepSeekResponse> responseEntity;
        try {
            responseEntity = restTemplate.postForEntity(url, entity, DeepSeekResponse.class);
        } catch (Exception e) {
            log.error("DeepSeek API 调用失败: {}", e.getMessage());
            throw new RuntimeException("AI 服务调用失败: " + e.getMessage(), e);
        }

        DeepSeekResponse response = responseEntity.getBody();

        // 检查 API 返回的业务错误
        if (response == null) {
            throw new RuntimeException("AI 服务返回空响应");
        }
        if (response.getError() != null) {
            DeepSeekResponse.ErrorDetail err = response.getError();
            log.error("DeepSeek API 返回错误: {} - {}", err.getCode(), err.getMessage());
            throw new RuntimeException("AI 服务错误: " + err.getMessage());
        }

        // 提取生成内容
        if (response.getChoices() == null || response.getChoices().isEmpty()) {
            throw new RuntimeException("AI 服务响应中无生成内容");
        }

        String content = response.getChoices().get(0).getMessage().getContent();
        if (content == null || content.isBlank()) {
            throw new RuntimeException("AI 服务生成内容为空");
        }

        // 记录 token 用量
        if (response.getUsage() != null) {
            log.info("Token 用量 - 输入: {}, 输出: {}, 总计: {}",
                    response.getUsage().getPromptTokens(),
                    response.getUsage().getCompletionTokens(),
                    response.getUsage().getTotalTokens());
        }

        return content;
    }

    // ==================== 响应解析 ====================

    /** 匹配 &lt;file path="..."&gt;...&lt;/file&gt; 的正则 */
    private static final Pattern FILE_BLOCK_PATTERN = Pattern.compile(
            "<file\\s+path=\"([^\"]+)\">\\s*([\\s\\S]*?)\\s*</file>",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 解析 AI 返回的文本，提取 &lt;file&gt; 块
     */
    private GenerateResult parseResponse(String responseContent) {
        List<SourceFile> files = new ArrayList<>();
        Matcher matcher = FILE_BLOCK_PATTERN.matcher(responseContent);

        while (matcher.find()) {
            String filePath = matcher.group(1).trim();
            String content = matcher.group(2).trim();

            // 标准化文件路径分隔符
            filePath = filePath.replace("\\", "/");

            // 校验路径是否以 src/main/java/ 开头
            if (!filePath.startsWith("src/main/java/")) {
                log.warn("跳过不符合路径规范的文件: {}", filePath);
                continue;
            }

            files.add(new SourceFile(filePath, content));
        }

        if (files.isEmpty()) {
            log.warn("未能从 AI 返回中解析到任何合法代码文件");
            return GenerateResult.builder()
                    .success(false)
                    .files(Collections.emptyList())
                    .rawResponse(responseContent)
                    .errorMessage("AI 返回格式不符合预期，未能提取到代码文件。原始返回内容已保留。")
                    .build();
        }

        log.info("成功解析出 {} 个代码文件", files.size());
        return GenerateResult.builder()
                .success(true)
                .files(files)
                .rawResponse(responseContent)
                .build();
    }
}
