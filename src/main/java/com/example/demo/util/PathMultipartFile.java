package com.example.demo.util;

import org.jetbrains.annotations.NotNull;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class PathMultipartFile implements MultipartFile {
    private final Path path;
    private final byte[] content;

    public PathMultipartFile(Path path) throws IOException {
        this.path = path;
        this.content = Files.readAllBytes(path);
    }

    @Override @NonNull public String getName() { return "file"; }
    @Override public String getOriginalFilename() { return path.getFileName().toString(); }
    @Override public String getContentType() { return "application/java-archive"; }
    @Override public boolean isEmpty() { return content.length == 0; }
    @Override public long getSize() { return content.length; }
    @Override public byte @NotNull [] getBytes() { return content; }
    @Override @NonNull public InputStream getInputStream() { return new ByteArrayInputStream(content); }
    @Override public void transferTo(@NonNull File dest) throws IOException {
        Files.copy(path, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}