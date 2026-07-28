package com.example.demo.ai.ai.util;

import com.example.demo.ai.ai.pojo.entity.AIChatMessage;
import com.example.demo.ai.ai.pojo.entity.Code;
import com.example.demo.pojo.entity.plugin.PluginInfo;
import com.example.demo.pojo.entity.plugin.PluginVersion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@Component
public class CompileUtil {

    @Value("${plugin.compiler.work-dir}")
    private String workDir;
    @Value("${plugin.compiler.mvn-command:mvn}")
    private String mvnCommand;

    /**
     * 创建代码文件。
     * @param codes 代码列表
     * @param message 会话消息记录（含 pom 依赖）
     */
    public void createCodeFile(List<Code> codes, AIChatMessage message) throws Exception {
        // 创建插件目录
        Path pluginDir = Path.of(workDir, message.getConversationId());
        // 先删除旧目录（如果存在）
        if (Files.exists(pluginDir)) {
            deleteDirectoryRecursively(pluginDir);
        }
        Files.createDirectories(pluginDir);

        for (Code code : codes) {
            String rawPath = code.getPath();
            String content = code.getContent();

            // 分离扩展名，只替换包名部分中的 .
            int lastDot = rawPath.lastIndexOf('.');
            if (lastDot > 0) {
                String packagePart = rawPath.substring(0, lastDot);
                String extPart = rawPath.substring(lastDot);  // 含点，如 ".java"
                rawPath = packagePart.replace(".", File.separator) + extPart;
            }

            Path filePath = pluginDir.resolve(rawPath);
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, content);
        }

        // 生成 pom.xml
        String pomContent = message.getPom();
        if (pomContent != null && !pomContent.isBlank()) {
            Path pomPath = pluginDir.resolve("pom.xml");
            Files.writeString(pomPath, pomContent);
        }
    }

    /**
     * 编译代码。
     * @param conversationId 会话消息id
     * @return 编译产物 JAR 的路径
     */
    public Path compileCode(String conversationId) throws Exception {
        Path projectDir = Path.of(workDir, conversationId);

        // 1. 确认目录存在
        if (!Files.exists(projectDir)) {
            throw new RuntimeException("项目目录不存在: " + projectDir);
        }

        // 2. 构建 Maven 编译命令
        ProcessBuilder pb = new ProcessBuilder(
                mvnCommand, "clean", "package", "-DskipTests", "-B"
        );
        pb.directory(projectDir.toFile());
        pb.redirectErrorStream(true);  // 合并 stderr → stdout

        log.info("开始编译: {}", projectDir);
        Process process = pb.start();

        // 3. 读取全部输出
        String output;
        try (var in = process.getInputStream()) {
            output = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        int exitCode = process.waitFor();
        log.info("编译完成, exitCode={}", exitCode);

        // 4. 失败 → 提取错误信息并抛出
        if (exitCode != 0) {
            String errors = extractCompileErrors(output);
            log.error("编译失败:\n{}", errors);
            throw new RuntimeException("编译失败:\n" + errors);
        }

        // 5. 成功 → 找到 target 目录下的 JAR
        Path targetDir = projectDir.resolve("target");
        try (var files = Files.list(targetDir)) {
            return files
                    .filter(f -> f.getFileName().toString().endsWith(".jar"))
                    .filter(f -> !f.getFileName().toString().endsWith("-sources.jar"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("未找到编译产物 JAR"));
        }
    }

    /**
     * 构建插件信息。
     * @param aiChatMessage 会话消息
     * @return 插件信息
     */
    public PluginInfo getPluginInfo(AIChatMessage aiChatMessage) {
        PluginInfo pluginInfo = new PluginInfo();

        // 新建 vs 更新：有 pluginId → 更新已有插件；无 pluginId → 新建
        if (aiChatMessage.getPluginId() != null && !aiChatMessage.getPluginId().isBlank()) {
            pluginInfo.setId(aiChatMessage.getPluginId());
        }
        pluginInfo.setName(aiChatMessage.getPluginName());
        pluginInfo.setDescription(aiChatMessage.getPluginDescription());
        pluginInfo.setAuthorId(aiChatMessage.getUserId());
        pluginInfo.setIsPublic(aiChatMessage.getIsPublic() != null ? aiChatMessage.getIsPublic() : false);

        // 2. 组装 PluginVersion
        PluginVersion pluginVersion = new PluginVersion();
        pluginVersion.setVersion(aiChatMessage.getVersion());
        pluginVersion.setChangelog(aiChatMessage.getChangelog());
        pluginVersion.setEntityPackage("entity");
        pluginVersion.setMethodPackage("service");
        pluginInfo.setPluginVersionList(List.of(pluginVersion));
        return pluginInfo;
    }

    /**
     * 从 Maven 输出中提取错误信息。
     * 只保留 [ERROR] 开头的关键行，过滤掉无意义的堆栈跟踪。
     */
    private String extractCompileErrors(String mavenOutput) {
        StringBuilder sb = new StringBuilder();
        boolean inCompilationError = false;

        for (String line : mavenOutput.lines().toList()) {
            if (line.contains("[ERROR]")) {
                // 跳过纯堆栈跟踪行（以 "at " 开头）
                if (line.trim().startsWith("at ") || line.contains("Caused by:")) {
                    continue;
                }
                sb.append(line).append("\n");
                inCompilationError = true;
            } else if (inCompilationError && line.trim().startsWith("[")) {
                // 遇到下一个日志级别标记，结束错误区域
                inCompilationError = false;
            }
        }

        // 如果没提取到有效错误，返回尾部输出（兜底）
        if (sb.isEmpty()) {
            String[] lines = mavenOutput.lines().toArray(String[]::new);
            int start = Math.max(0, lines.length - 30);
            for (int i = start; i < lines.length; i++) {
                sb.append(lines[i]).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * 递归删除目录
     */
    public void deleteDirectoryRecursively(Path dir) throws IOException {
        if (Files.isDirectory(dir)) {
            try (var stream = Files.newDirectoryStream(dir)) {
                for (Path entry : stream) {
                    deleteDirectoryRecursively(entry);
                }
            }
        }
        Files.deleteIfExists(dir);
    }
}
