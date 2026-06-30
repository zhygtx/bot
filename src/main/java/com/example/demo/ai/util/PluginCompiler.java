package com.example.demo.ai.util;

import com.example.demo.ai.pojo.dto.Dependency;
import com.example.demo.ai.pojo.dto.SourceFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Maven 编译打包工具。<br>
 * 将 AI 生成的源码文件编译打包为 JAR，供插件上传使用。
 */
@Slf4j
@Component
public class PluginCompiler {

    /** 编译工作目录基路径 */
    @Value("${ai.plugin.compiler.work-dir:}")
    private String workDirBase;

    /** 编译超时（秒） */
    @Value("${ai.plugin.compiler.timeout-seconds:120}")
    private int timeoutSeconds;

    /** Maven 安装路径（可选，默认使用系统 PATH 中的 mvn） */
    @Value("${ai.plugin.compiler.maven-home:}")
    private String mavenHome;

    /**
     * 编译结果
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompileResult {
        /** 是否编译成功 */
        private boolean success;
        /** 编译产物 JAR 的绝对路径（成功时） */
        private String jarPath;
        /** 错误信息列表（失败时） */
        private List<String> errors;
        /** Maven 完整输出日志 */
        private String outputLog;
        /** 编译工作目录路径（用于后续清理） */
        private String workDir;
    }

    /**
     * 编译源码并打包 JAR。<br>
     * 注意：编译成功后，调用方需在使用完 JAR 后调用 {@link #cleanup(CompileResult)} 清理临时目录。
     *
     * @param sourceFiles 待编译的源码文件列表
     * @param dependencies AI 声明的额外 Maven 依赖（可为空列表）
     * @return 编译结果
     */
    public CompileResult compile(List<SourceFile> sourceFiles, List<Dependency> dependencies) {
        // 创建 UUID 隔离的独立工作目录
        Path workDir = createWorkDir();
        if (workDir == null) {
            return CompileResult.builder()
                    .success(false)
                    .errors(Collections.singletonList("无法创建工作目录"))
                    .build();
        }

        try {
            // 写入 pom.xml（含依赖注入）
            writePomXml(workDir, dependencies != null ? dependencies : Collections.emptyList());

            // 写入源码文件
            writeSourceFiles(workDir, sourceFiles);

            // 执行 Maven 编译
            CompileResult result = executeMaven(workDir);

            if (result.isSuccess()) {
                result.setWorkDir(workDir.toAbsolutePath().toString());
            } else {
                deleteDirectory(workDir);
            }

            return result;

        } catch (Exception e) {
            log.error("编译过程发生异常", e);
            deleteDirectory(workDir);
            return CompileResult.builder()
                    .success(false)
                    .errors(Collections.singletonList("编译异常: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * 清理编译产生的临时文件和目录。<br>
     * 编译成功后调用此方法清理。编译失败时会自动清理，无需调用。
     *
     * @param result 编译结果（必须是由当前实例返回的结果）
     */
    public void cleanup(CompileResult result) {
        if (result != null && result.getWorkDir() != null) {
            deleteDirectory(Path.of(result.getWorkDir()));
            log.debug("已清理编译工作目录: {}", result.getWorkDir());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 创建 UUID 隔离的独立工作目录
     */
    private Path createWorkDir() {
        try {
            String base = workDirBase;
            if (base == null || base.isBlank()) {
                base = System.getProperty("java.io.tmpdir") + File.separator + "ai-plugin-build";
            }
            String uuid = UUID.randomUUID().toString().replace("-", "");
            Path dir = Path.of(base, uuid);
            Files.createDirectories(dir);
            log.info("创建编译工作目录: {}", dir);
            return dir;
        } catch (IOException e) {
            log.error("创建工作目录失败", e);
            return null;
        }
    }

    /**
     * 将模板 pom.xml 写入工作目录，并注入 AI 声明的额外依赖
     */
    private void writePomXml(Path workDir, List<Dependency> dependencies) throws IOException {
        ClassPathResource resource = new ClassPathResource("ai-plugin-template/pom.xml");
        if (!resource.exists()) {
            throw new IOException("找不到 pom.xml 模板文件: classpath:ai-plugin-template/pom.xml");
        }

        // 读取模板内容
        String templateContent;
        try (var is = resource.getInputStream()) {
            templateContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        // 构造依赖 XML 片段并替换占位符
        String depXml = buildDependencyXml(dependencies);
        String pomContent = templateContent.replace("<!--AI_DEPENDENCIES-->", depXml);

        Files.writeString(workDir.resolve("pom.xml"), pomContent, StandardCharsets.UTF_8);
        log.debug("已写入 pom.xml，注入 {} 个额外依赖", dependencies.size());
    }

    /**
     * 构造 Maven 依赖 XML 片段
     */
    private String buildDependencyXml(List<Dependency> dependencies) {
        if (dependencies == null || dependencies.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Dependency dep : dependencies) {
            sb.append("        <dependency>\n");
            sb.append("            <groupId>").append(dep.getGroupId()).append("</groupId>\n");
            sb.append("            <artifactId>").append(dep.getArtifactId()).append("</artifactId>\n");
            sb.append("            <version>").append(dep.getVersion()).append("</version>\n");
            String scope = dep.getScope() != null && !dep.getScope().isBlank()
                    ? dep.getScope() : "compile";
            sb.append("            <scope>").append(scope).append("</scope>\n");
            sb.append("        </dependency>\n");
        }
        return sb.toString();
    }

    /**
     * 将源码文件写入工作目录的对应包路径下
     */
    private void writeSourceFiles(Path workDir, List<SourceFile> sourceFiles) throws IOException {
        for (SourceFile sourceFile : sourceFiles) {
            Path targetFile = workDir.resolve(sourceFile.getFilePath());
            Files.createDirectories(targetFile.getParent());
            Files.writeString(targetFile, sourceFile.getContent(), StandardCharsets.UTF_8);
            log.debug("已写入: {}", sourceFile.getFilePath());
        }
    }

    /**
     * 执行 Maven 编译打包
     */
    private CompileResult executeMaven(Path workDir) throws Exception {
        // 构建 mvn 命令
        List<String> command = buildMvnCommand(workDir);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workDir.toFile());
        pb.redirectErrorStream(true);

        log.info("执行 Maven 命令: {}", String.join(" ", command));

        Process process = pb.start();

        // 读取输出（使用独立线程避免缓冲区阻塞）
        StringBuilder outputBuilder = new StringBuilder();
        Thread readerThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputBuilder.append(line).append("\n");
                }
            } catch (IOException e) {
                log.warn("读取 Maven 输出流异常", e);
            }
        });
        readerThread.setDaemon(true);
        readerThread.start();

        // 等待编译完成（带超时）
        boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
        readerThread.join(2000);

        if (!finished) {
            process.destroyForcibly();
            log.warn("Maven 编译超时（{} 秒）", timeoutSeconds);
            return CompileResult.builder()
                    .success(false)
                    .errors(Collections.singletonList("编译超时（超过 " + timeoutSeconds + " 秒），请检查网络或 Maven 配置"))
                    .outputLog(outputBuilder.toString())
                    .build();
        }

        String output = outputBuilder.toString();
        int exitCode = process.exitValue();

        if (exitCode == 0) {
            // 编译成功，查找 JAR 文件
            Path jarFile = findJar(workDir);
            if (jarFile != null) {
                log.info("编译成功，JAR: {}", jarFile);
                return CompileResult.builder()
                        .success(true)
                        .jarPath(jarFile.toAbsolutePath().toString())
                        .outputLog(output)
                        .build();
            } else {
                log.warn("编译成功但未找到 JAR 文件");
                return CompileResult.builder()
                        .success(false)
                        .errors(Collections.singletonList("编译成功但未找到 JAR 文件"))
                        .outputLog(output)
                        .build();
            }
        } else {
            // 编译失败，解析错误信息
            List<String> errors = parseErrors(output);
            log.warn("编译失败，{} 个错误", errors.size());
            return CompileResult.builder()
                    .success(false)
                    .errors(errors)
                    .outputLog(output)
                    .build();
        }
    }

    /**
     * 构建 Maven 命令
     */
    private List<String> buildMvnCommand(Path workDir) {
        List<String> command = new ArrayList<>();

        if (mavenHome != null && !mavenHome.isBlank()) {
            // 指定了 Maven 安装路径
            String mvn = mavenHome + File.separator + "bin" + File.separator + "mvn";
            // 在 Windows 下添加 .cmd 后缀
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                mvn += ".cmd";
            }
            command.add(mvn);
        } else {
            // 使用系统 PATH 中的 mvn
            command.add("mvn");
        }

        command.add("clean");
        command.add("package");
        command.add("-f");
        command.add(workDir.resolve("pom.xml").toString());
        command.add("-q"); // 安静模式，减少冗余输出

        return command;
    }

    /**
     * 在 target 目录下查找编译产物 JAR
     */
    private Path findJar(Path workDir) throws IOException {
        Path targetDir = workDir.resolve("target");
        if (!Files.isDirectory(targetDir)) {
            return null;
        }
        try (var stream = Files.list(targetDir)) {
            return stream.filter(p -> p.toString().endsWith(".jar")
                            && !p.toString().endsWith("-sources.jar")
                            && !p.toString().endsWith("-javadoc.jar"))
                    .findFirst()
                    .orElse(null);
        }
    }

    /**
     * 从 Maven 输出中提取错误行
     */
    private List<String> parseErrors(String output) {
        List<String> errors = new ArrayList<>();
        StringBuilder currentError = new StringBuilder();

        for (String line : output.split("\n")) {
            if (line.contains("[ERROR]")) {
                // 跳过 Maven 自身的执行摘要行
                if (line.contains("Failed to execute goal")
                        || line.contains("To see the full stack trace")
                        || line.contains("[help 1]")
                        || line.contains("[help 2]")) {
                    continue;
                }
                String errorContent = line.substring(line.indexOf("[ERROR]") + 7);
                String trimmedError = errorContent.trim();
                if (!trimmedError.isEmpty()) {
                    // 判断是否是多行错误的续行（缩进或以 " 符号:" 开头）
                    boolean isContinuation = errorContent.startsWith(" ") || errorContent.startsWith("\t")
                            || errorContent.startsWith("  symbol")
                            || errorContent.startsWith("  location")
                            || (currentError.length() > 0 && !trimmedError.contains(":"));
                    if (isContinuation) {
                        if (currentError.length() > 0) {
                            currentError.append(" ").append(trimmedError);
                        }
                    } else {
                        if (currentError.length() > 0) {
                            errors.add(currentError.toString());
                        }
                        currentError = new StringBuilder(trimmedError);
                    }
                }
            }
        }
        if (currentError.length() > 0) {
            errors.add(currentError.toString());
        }

        // 如果没有提取到具体错误，保留完整输出作为错误信息
        if (errors.isEmpty()) {
            // 尝试取最后几行非空输出
            String[] lines = output.split("\n");
            StringBuilder lastLines = new StringBuilder();
            int count = 0;
            for (int i = lines.length - 1; i >= 0 && count < 5; i--) {
                String trimmed = lines[i].trim();
                if (!trimmed.isEmpty()) {
                    lastLines.insert(0, trimmed + "\n");
                    count++;
                }
            }
            errors.add(lastLines.toString().trim());
        }

        return errors;
    }

    /**
     * 递归删除目录
     */
    private void deleteDirectory(Path path) {
        if (path == null || !Files.exists(path)) {
            return;
        }
        try (var walk = Files.walk(path)) {
            walk.sorted(java.util.Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException e) {
                            log.warn("删除临时文件失败: {}", p, e);
                        }
                    });
        } catch (IOException e) {
            log.warn("清理编译目录失败: {}", path, e);
        }
    }
}
