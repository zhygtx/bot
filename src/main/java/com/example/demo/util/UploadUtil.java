package com.example.demo.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
public class UploadUtil {

    @Value("${upload.plugin-path}")
    private String pluginPath;

    /**
     * 上传插件文件
     * @param file 插件文件
     * @param authorId 插件作者ID
     * @return 插件存储路径
     */
    public String uploadPlugin(MultipartFile file, String authorId) {
        //1 检查路径是否存在
        return null;
    }

    /**
     * 创建目录
     */
    private void createDirectories(String path) {
        try {
            Path directory = Paths.get(path);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
                log.info("创建目录: {}", path);
            }
        } catch (IOException e) {
            log.error("创建目录失败: {}", path, e);
        }
    }
}
