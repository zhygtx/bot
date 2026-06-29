package com.example.demo.ai.util;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.nio.file.Files.write;

/**
 * 基于字节数组的 MultipartFile 实现，用于将本地 JAR 文件传入 {@code PluginService.add()}。
 */
public class ByteArrayMultipartFile implements MultipartFile {

    private final String name;
    private final String originalFilename;
    private final byte[] content;

    public ByteArrayMultipartFile(String name, String originalFilename, byte[] content) {
        this.name = name;
        this.originalFilename = originalFilename;
        this.content = content;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public String getOriginalFilename() {
        return originalFilename;
    }

    @Override
    public String getContentType() {
        return "application/java-archive";
    }

    @Override
    public boolean isEmpty() {
        return content == null || content.length == 0;
    }

    @Override
    public long getSize() {
        return content != null ? content.length : 0;
    }

    @Override
    public byte @NotNull [] getBytes() {
        return content;
    }

    @Override
    public @NotNull InputStream getInputStream() {
        return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
        write(dest.toPath(), content);
    }
}
