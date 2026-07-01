package com.example.demo.ai.ai.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class AIUtil {

    // ==================== 正则模式：代码响应解析 ====================

    private static final Pattern DEPS_BLOCK = Pattern.compile(
            "<dependencies>(.*?)</dependencies>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern DEP_ELEMENT = Pattern.compile(
            "<groupId>(.+?)</groupId>\\s*" +
            "<artifactId>(.+?)</artifactId>\\s*" +
            "<version>(.+?)</version>" +
            "(?:\\s*<scope>(.+?)</scope>)?",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern PLUGIN_BLOCK = Pattern.compile(
            "<plugin>(.*?)</plugin>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern PLUGIN_ELEMENT = Pattern.compile(
            "<(name|description)>(.*?)</\\1>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    // ==================== 模板加载 ====================

    /**
     * 载入提示词模板文件
     */
    public static String loadTemplate(String templatePath) throws IOException {
        String path = templatePath;
        if (path.startsWith("classpath:")) {
            path = path.substring("classpath:".length());
        }
        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            throw new IOException("找不到提示词模板文件: " + templatePath);
        }
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    // ==================== 批量解析 ====================

    /**
     * 从 AI 响应文本中解析 &lt;dependencies&gt; 块。
     * @return 依赖列表，每个元素为 {groupId, artifactId, version, scope}
     */
    public static List<Map<String, String>> parseDependencies(String text) {
        List<Map<String, String>> result = new ArrayList<>();
        Matcher blockMatcher = DEPS_BLOCK.matcher(text);
        if (!blockMatcher.find()) return result;

        String depsContent = blockMatcher.group(1);
        Matcher depMatcher = DEP_ELEMENT.matcher(depsContent);
        while (depMatcher.find()) {
            Map<String, String> dep = new LinkedHashMap<>();
            dep.put("groupId", depMatcher.group(1).trim());
            dep.put("artifactId", depMatcher.group(2).trim());
            dep.put("version", depMatcher.group(3).trim());
            dep.put("scope", depMatcher.group(4) != null ? depMatcher.group(4).trim() : "compile");
            result.add(dep);
        }
        return result;
    }

    /**
     * 从 AI 响应文本中解析 &lt;plugin&gt; 块。
     * @return {"name": "...", "description": "..."}，无 plugin 块时返回空 Map
     */
    public static Map<String, String> parsePluginInfo(String text) {
        Map<String, String> result = new LinkedHashMap<>();
        Matcher blockMatcher = PLUGIN_BLOCK.matcher(text);
        if (!blockMatcher.find()) return result;

        String pluginContent = blockMatcher.group(1);
        Matcher elementMatcher = PLUGIN_ELEMENT.matcher(pluginContent);
        while (elementMatcher.find()) {
            String key = elementMatcher.group(1).toLowerCase();
            String value = elementMatcher.group(2).trim();
            result.put(key, value);
        }
        return result;
    }

    // ==================== 流式调用 + 增量解析 ====================

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 流式调用 AI 并同步完成：打字机推送 + 增量解析 + 构建 code JSON。
     *
     * @param chatClient    Spring AI 客户端
     * @param springMessages 构建好的消息列表
     * @param onEvent       事件回调，(事件类型, 数据) → 推给前端
     * @return {"rawText": AI 完整回复, "codeJson": 解析后的结构化 JSON}
     * {
     *      *   "dependencies": [
     *      *     {"groupId": "com.google.code.gson", "artifactId": "gson", "version": "2.11.0", "scope": "compile"}
     *      *   ],
     *      *   "pluginName": "文本处理工具",
     *      *   "pluginDescription": "提供字符串拼接功能",
     *      *   "files": [
     *      *     {"path": "src/main/java/com/example/entity/UserData.java", "content": "package com.example.entity;\n..."},
     *      *     {"path": "src/main/java/com/example/service/TextService.java", "content": "package com.example.service;\n..."}
     *      *   ]
     *      * }
     */
    public static Map<String, String> streamAndParse(ChatClient chatClient,
                                                      List<Message> springMessages,
                                                      BiConsumer<String, Object> onEvent) {
        StringBuilder fullContent = new StringBuilder();

        // 增量解析状态
        final int[] consumedPos = {0};
        final String[] currentFilePath = {null};
        final StringBuilder[] currentFileContent = {null};
        final List<Map<String, String>> codeFiles = new ArrayList<>();

        // 流式调用
        chatClient.prompt(new Prompt(springMessages))
                .stream()
                .content()
                .doOnNext(chunk -> {
                    fullContent.append(chunk);

                    // 1. 打字机效果：8 字分块推送
                    if (onEvent != null) {
                        int cursor = 0, size = 8;
                        while (cursor < chunk.length()) {
                            int end = Math.min(cursor + size, chunk.length());
                            onEvent.accept("delta", chunk.substring(cursor, end));
                            cursor = end;
                        }
                    }

                    // 2. 增量解析：扫描新增文本，检测标签
                    consumedPos[0] = parseIncremental(
                            fullContent.toString(), consumedPos[0],
                            currentFilePath, currentFileContent,
                            codeFiles, onEvent);
                })
                .blockLast();

        // 流结束后，批量解析依赖和插件信息
        String rawText = fullContent.toString();
        List<Map<String, String>> codeDeps = parseDependencies(rawText);
        Map<String, String> pluginInfo = parsePluginInfo(rawText);

        // 序列化 code JSON
        Map<String, Object> codeWrapper = new LinkedHashMap<>();
        codeWrapper.put("dependencies", codeDeps);
        codeWrapper.put("pluginName", pluginInfo.get("name"));
        codeWrapper.put("pluginDescription", pluginInfo.get("description"));
        codeWrapper.put("files", codeFiles);

        String codeJson;
        try {
            codeJson = MAPPER.writeValueAsString(codeWrapper);
        } catch (Exception e) {
            log.error("序列化 code JSON 失败", e);
            codeJson = "{}";
        }

        Map<String, String> result = new LinkedHashMap<>();
        result.put("rawText", rawText);
        result.put("codeJson", codeJson);
        return result;
    }

    /**
     * 增量扫描文本中新增部分，识别 {@code <file path="...">} 和 {@code </file>} 标签，
     * 实时发送通知并收集文件内容。
     */
    private static int parseIncremental(String text, int start,
                                         String[] currentFilePath,
                                         StringBuilder[] currentFileContent,
                                         List<Map<String, String>> codeFiles,
                                         BiConsumer<String, Object> onEvent) {
        int i = start;
        int len = text.length();

        while (i < len) {
            int tagStart = text.indexOf('<', i);
            if (tagStart == -1) {
                if (currentFilePath[0] != null && currentFileContent[0] != null) {
                    currentFileContent[0].append(text, i, len);
                }
                return len;
            }

            if (currentFilePath[0] != null && currentFileContent[0] != null && tagStart > i) {
                currentFileContent[0].append(text, i, tagStart);
            }

            // ── <file path="..." ──
            if (text.startsWith("<file path=\"", tagStart)) {
                int pathStart = tagStart + "<file path=\"".length();
                int pathEnd = text.indexOf('"', pathStart);
                if (pathEnd == -1) return tagStart;
                int tagEnd = text.indexOf('>', pathEnd);
                if (tagEnd == -1) return tagStart;

                currentFilePath[0] = text.substring(pathStart, pathEnd);
                currentFileContent[0] = new StringBuilder();

                if (onEvent != null) {
                    onEvent.accept("message_change", "正在生成文件: " + currentFilePath[0]);
                }
                i = tagEnd + 1;
                continue;
            }

            // ── </file> ──
            if (text.startsWith("</file>", tagStart)) {
                if (currentFilePath[0] != null) {
                    Map<String, String> fileEntry = new LinkedHashMap<>();
                    fileEntry.put("path", currentFilePath[0]);
                    fileEntry.put("content", currentFileContent[0] != null
                            ? currentFileContent[0].toString() : "");
                    codeFiles.add(fileEntry);

                    currentFilePath[0] = null;
                    currentFileContent[0] = null;
                }
                i = tagStart + "</file>".length();
                continue;
            }

            // ── 其他标签，跳过 ──
            int tagEnd = text.indexOf('>', tagStart);
            if (tagEnd == -1) return tagStart;
            i = tagEnd + 1;
        }
        return i;
    }
}
