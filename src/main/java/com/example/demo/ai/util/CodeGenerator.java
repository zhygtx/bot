package com.example.demo.ai.util;

import com.example.demo.ai.pojo.dto.Dependency;
import com.example.demo.ai.pojo.dto.SourceFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
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

    @Value("${ai.plugin.prompt-template-path}")
    private String pluginPromptTemplatePath;

    // ==================== 审查配置字段 ====================

    @Value("${ai.review.enabled}")
    private boolean reviewEnabled;

    @Value("${ai.review.prompt-template-path}")
    private String reviewPromptTemplatePath;

    @Value("${ai.review.model}")
    private String reviewModel;

    private final ChatClient chatClient;

    public CodeGenerator(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
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
        /** AI 声明的额外 Maven 依赖 */
        @Builder.Default
        private List<Dependency> dependencies = Collections.emptyList();
        /** AI 产出的插件名称 */
        private String pluginName;
        /** AI 产出的插件描述 */
        private String pluginDescription;
        /** 审查结果（审查关闭时为 null） */
        private ReviewResult reviewResult;
        /** AI 返回的原始文本（用于调试或格式异常时回退） */
        private String rawResponse;
        /** 错误信息 */
        private String errorMessage;
    }

    /**
     * 代码审查结果
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewResult {
        private boolean passed;
        private String summary;
        @Builder.Default
        private List<ReviewIssue> issues = Collections.emptyList();
    }

    /**
     * 审查发现的具体问题
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewIssue {
        private String severity;
        private String file;
        private Integer line;
        private String message;
        private String suggestion;
    }

    // ==================== 公共方法 ====================

    /**
     * 流式生成代码。onDelta 会收到模型增量文本；方法返回完整解析结果。
     */

    public GenerateResult generateCodeStream(List<Message> historyMessages,
                                             String requirements,
                                             String entityPackage,
                                             String methodPackage,
                                             String existingCodeJson,
                                             Consumer<String> onDelta) {
        try {
            List<Message> messages = buildMessages(historyMessages, requirements, entityPackage, methodPackage, existingCodeJson);
            String responseContent = callSpringAiStream(messages, onDelta);
            return parseResponse(responseContent);
        } catch (Exception e) {
            log.error("AI 流式代码生成异常", e);
            return GenerateResult.builder()
                    .success(false)
                    .errorMessage("AI 代码生成失败: " + e.getMessage())
                    .build();
        }
    }

    // ==================== 代码审查 ====================

    /**
     * 审查生成的代码。
     *
     * @param files 生成的源码文件
     * @return 审查结果（审查关闭时返回 null，审查失败时降级为 passed=false 结果）
     */
    public ReviewResult reviewCode(List<SourceFile> files) {
        if (!reviewEnabled) {
            log.info("代码审查已关闭，跳过审查");
            return null;
        }
        if (files == null || files.isEmpty()) {
            return null;
        }
        // 拼接所有源码
        String code = buildCodeForReview(files);

        try {
            String reviewPrompt = loadReviewTemplate();
            reviewPrompt = reviewPrompt.replace("{{code}}", code);

            String response = callDeepSeekForReview(reviewPrompt);
            return parseReviewResponse(response);
        } catch (Exception e) {
            log.warn("代码审查调用失败，降级处理", e);
            return ReviewResult.builder()
                    .passed(false)
                    .summary("审查服务暂时不可用: " + e.getMessage())
                    .build();
        }
    }

    /**
     * 将所有源码拼接为审查用的文本
     */
    private String buildCodeForReview(List<SourceFile> files) {
        StringBuilder sb = new StringBuilder();
        for (SourceFile file : files) {
            sb.append("// File: ").append(file.getFilePath()).append("\n");
            sb.append(file.getContent()).append("\n\n");
        }
        return sb.toString();
    }

    /**
     * 从 classpath 加载审查提示词模板
     */
    private String loadReviewTemplate() throws IOException {
        String path = reviewPromptTemplatePath;
        if (path.startsWith("classpath:")) {
            path = path.substring("classpath:".length());
        }
        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            throw new IOException("找不到审查提示词模板文件: " + reviewPromptTemplatePath);
        }
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    /**
     * 调用 DeepSeek API 进行代码审查
     */
    private String callDeepSeekForReview(String prompt) {
        try {
            return chatClient.prompt()
                    .system("你是一个 Java 代码安全审查专家，只输出 JSON 格式的审查结果。")
                    .user(prompt)
                    .options(DeepSeekChatOptions.builder().model(reviewModel).build())
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("Spring AI 审查调用失败: {}", e.getMessage());
            throw new RuntimeException("审查服务调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析审查 API 返回的 JSON
     */
    private ReviewResult parseReviewResponse(String response) {
        try {
            // 提取 JSON（AI 可能前后有多余文本）
            int jsonStart = response.indexOf("{");
            int jsonEnd = response.lastIndexOf("}") + 1;
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                String json = response.substring(jsonStart, jsonEnd);
                return OBJECT_MAPPER.readValue(json, ReviewResult.class);
            }
            log.warn("审查返回中未找到有效 JSON，原始内容: {}", response);
        } catch (Exception e) {
            log.warn("解析审查结果 JSON 失败: {}", e.getMessage());
        }
        return ReviewResult.builder()
                .passed(false)
                .summary("审查结果解析失败，原始返回: " + response)
                .build();
    }

    // ==================== 提示词构造 ====================

    /**
     * 加载提示词模板并替换占位符
     */
    private List<Message> buildMessages(List<Message> historyMessages,
                                        String requirements,
                                        String entityPackage,
                                        String methodPackage,
                                        String existingCodeJson) throws IOException {
        // 加载模板
        String template = loadTemplate();

        String prompt = template
                .replace("{{entityPackage}}", entityPackage)
                .replace("{{methodPackage}}", methodPackage)
                .replace("{{requirements}}", requirements);

        prompt = prompt.replaceAll("\\{\\{#existingCode}}[\\s\\S]*?\\{\\{/existingCode}}", "");

        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(prompt));
        if (historyMessages != null && !historyMessages.isEmpty()) {
            messages.addAll(historyMessages);
        }
        messages.add(new UserMessage(buildUserMessage(requirements, entityPackage, methodPackage, existingCodeJson)));
        return messages;
    }

    /**
     * 从 classpath 加载提示词模板
     */
    private String loadTemplate() throws IOException {
        String path = pluginPromptTemplatePath;
        if (path.startsWith("classpath:")) {
            path = path.substring("classpath:".length());
        }
        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            throw new IOException("找不到提示词模板文件: " + pluginPromptTemplatePath);
        }
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    // ==================== DeepSeek API 调用 ====================

    private String callSpringAiStream(List<Message> messages, Consumer<String> onDelta) {
        StringBuilder fullContent = new StringBuilder();
        chatClient.prompt(new Prompt(messages))
                .stream()
                .content()
                .doOnNext(delta -> {
                    if (delta == null || delta.isEmpty()) {
                        return;
                    }
                    fullContent.append(delta);
                    if (onDelta != null) {
                        streamDelta(delta, onDelta);
                    }
                })
                .blockLast();
        return fullContent.toString();
    }

    private String buildUserMessage(String requirements,
                                   String entityPackage,
                                   String methodPackage,
                                   String existingCodeJson) {
        StringBuilder sb = new StringBuilder();
        sb.append("当前生成参数：\n");
        sb.append("- 实体类包：").append(entityPackage).append('\n');
        sb.append("- 方法类包：").append(methodPackage).append('\n');
        sb.append("- 用户需求：").append(requirements).append('\n');
        if (existingCodeJson != null && !existingCodeJson.isBlank()) {
            sb.append('\n');
            sb.append("当前已存在代码快照（请在此基础上修改，并保持历史轮次代码前后一致）：\n");
            sb.append(existingCodeJson).append('\n');
        }
        sb.append('\n');
        sb.append("请严格输出 `<progress>` 和 `<file>` 结构，优先保持每轮代码前后一致，");
        sb.append("如历史轮次已有代码，请将其视为当前上下文的一部分，不要丢失已有文件。");
        return sb.toString();
    }

    private void streamDelta(String delta, Consumer<String> onDelta) {
        int cursor = 0;
        int chunkSize = 8;
        while (cursor < delta.length()) {
            int end = Math.min(cursor + chunkSize, delta.length());
            onDelta.accept(delta.substring(cursor, end));
            cursor = end;
        }
    }

    // ==================== 响应解析 ====================

    /** 匹配 &lt;dependencies&gt;...&lt;/dependencies&gt; 块 */
    private static final Pattern DEPS_BLOCK_PATTERN = Pattern.compile(
            "<dependencies>(.*?)</dependencies>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    /** 匹配 &lt;plugin&gt;...&lt;/plugin&gt; 块 */
    private static final Pattern PLUGIN_BLOCK_PATTERN = Pattern.compile(
            "<plugin>(.*?)</plugin>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    /** 匹配插件发布信息子元素 */
    private static final Pattern PLUGIN_ELEMENT_PATTERN = Pattern.compile(
            "<(name|description)>(.*?)</\\1>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    /** 匹配 &lt;dependency&gt; 内的四个子元素 */
    private static final Pattern DEP_ELEMENT_PATTERN = Pattern.compile(
            "<groupId>(.+?)</groupId>\\s*" +
            "<artifactId>(.+?)</artifactId>\\s*" +
            "<version>(.+?)</version>" +
            "(?:\\s*<scope>(.+?)</scope>)?",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    /** 复用 ObjectMapper 实例 */
    private static final com.fasterxml.jackson.databind.ObjectMapper OBJECT_MAPPER =
            new com.fasterxml.jackson.databind.ObjectMapper();

    /** 匹配 &lt;file path="..."&gt;...&lt;/file&gt; 的正则 */
    private static final Pattern FILE_BLOCK_PATTERN = Pattern.compile(
            "<file\\s+path=\"([^\"]+)\">\\s*([\\s\\S]*?)\\s*</file>",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 解析 AI 返回的文本，先提取依赖，再提取代码文件
     */
    private GenerateResult parseResponse(String responseContent) {
        // Step 1: 提取依赖
        List<Dependency> dependencies = parseDependencies(responseContent);
        PluginMetadata metadata = parsePluginMetadata(responseContent);

        // Step 2: 提取代码文件
        List<SourceFile> files = new ArrayList<>();
        Matcher matcher = FILE_BLOCK_PATTERN.matcher(responseContent);

        while (matcher.find()) {
            String filePath = matcher.group(1).trim();
            String content = matcher.group(2).trim();

            filePath = filePath.replace("\\", "/");
            if (!isAllowedGeneratedPath(filePath)) {
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
                    .dependencies(dependencies)
                    .pluginName(metadata.name())
                    .pluginDescription(metadata.description())
                    .rawResponse(responseContent)
                    .errorMessage("AI 返回格式不符合预期，未能提取到代码文件。原始返回内容已保留。")
                    .build();
        }

        log.info("成功解析出 {} 个代码文件，{} 个依赖", files.size(), dependencies.size());
        return GenerateResult.builder()
                .success(true)
                .files(files)
                .dependencies(dependencies)
                .pluginName(metadata.name())
                .pluginDescription(metadata.description())
                .rawResponse(responseContent)
                .build();
    }

    private boolean isAllowedGeneratedPath(String filePath) {
        return filePath.startsWith("src/main/java/")
                || filePath.startsWith("src/main/resources/");
    }

    /**
     * 从 AI 返回中解析 &lt;dependencies&gt; 块
     */
    private List<Dependency> parseDependencies(String responseContent) {
        Matcher blockMatcher = DEPS_BLOCK_PATTERN.matcher(responseContent);
        if (!blockMatcher.find()) {
            return Collections.emptyList();
        }

        String depsContent = blockMatcher.group(1);
        Matcher depMatcher = DEP_ELEMENT_PATTERN.matcher(depsContent);
        List<Dependency> deps = new ArrayList<>();

        while (depMatcher.find()) {
            String groupId = depMatcher.group(1).trim();
            String artifactId = depMatcher.group(2).trim();
            String version = depMatcher.group(3).trim();
            String scope = depMatcher.group(4) != null ? depMatcher.group(4).trim() : "compile";

            deps.add(new Dependency(groupId, artifactId, version, scope));
            log.info("解析到依赖: {}:{}:{} (scope={})", groupId, artifactId, version, scope);
        }

        return deps;
    }

    private PluginMetadata parsePluginMetadata(String responseContent) {
        Matcher blockMatcher = PLUGIN_BLOCK_PATTERN.matcher(responseContent);
        if (!blockMatcher.find()) {
            return new PluginMetadata(null, null);
        }

        String pluginContent = blockMatcher.group(1);
        Matcher elementMatcher = PLUGIN_ELEMENT_PATTERN.matcher(pluginContent);
        String name = null;
        String description = null;
        while (elementMatcher.find()) {
            String key = elementMatcher.group(1).toLowerCase();
            String value = elementMatcher.group(2).trim();
            if ("name".equals(key)) {
                name = value;
            } else if ("description".equals(key)) {
                description = value;
            }
        }
        return new PluginMetadata(name, description);
    }

    private record PluginMetadata(String name, String description) {
    }
}
