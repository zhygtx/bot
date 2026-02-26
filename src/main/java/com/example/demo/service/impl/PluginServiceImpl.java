package com.example.demo.service.impl;

import com.example.demo.pojo.plugin.EntityInfo;
import com.example.demo.pojo.plugin.MethodClassInfo;
import com.example.demo.pojo.plugin.PluginInfo;
import com.example.demo.pojo.plugin.PluginVersion;
import com.example.demo.service.PluginService;
import com.example.demo.util.PluginUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class PluginServiceImpl implements PluginService {

    private final PluginUtil pluginUtil;

    public PluginServiceImpl(PluginUtil pluginUtil) {
        this.pluginUtil = pluginUtil;
    }

    @SneakyThrows
    @Override
    public PluginInfo analyzePlugin(PluginInfo pluginInfo) {
        // 从插件版本列表中获取第一个版本
        PluginVersion pluginVersion = pluginInfo.getPluginVersionList() != null && !pluginInfo.getPluginVersionList().isEmpty() 
                ? pluginInfo.getPluginVersionList().get(0) 
                : new PluginVersion();
        
        // 获取插件路径
        String jarPath = pluginVersion.getPath();
        File file = new File(jarPath);

        try {
            // 初始化PluginInfo对象
            if (pluginInfo.getId() == null) {
                pluginInfo.setId(UUID.randomUUID().toString());
            }
            if (pluginInfo.getName() == null) {
                // 从文件名提取插件名称
                String fileName = file.getName();
                pluginInfo.setName(fileName.replace(".jar", ""));
            }
            if (pluginInfo.getCreateTime() == null) {
                pluginInfo.setCreateTime(LocalDateTime.now());
            }
            pluginInfo.setUpdateTime(LocalDateTime.now());
            
            // 初始化插件版本信息
            if (pluginVersion.getId() == null) {
                pluginVersion.setId(UUID.randomUUID().toString());
                pluginVersion.setPluginId(pluginInfo.getId());
                pluginVersion.setVersion("1.0.0"); // 默认版本
                pluginVersion.setPath(jarPath);
                pluginVersion.setFileSize(file.length());
                // 计算文件MD5
                pluginVersion.setFileMd5(calculateMD5(file));
                pluginVersion.setCreateTime(LocalDateTime.now());
                pluginVersion.setChangelog("初始版本");
                pluginVersion.setCompatibleVersion("{}");
            }

            // 扫描实体类信息
            List<EntityInfo> entityInfos = pluginUtil.scanEntities(pluginVersion);
            pluginVersion.setEntityInfoList(entityInfos);

            // 扫描方法类信息
            List<MethodClassInfo> methodClassInfos = pluginUtil.scanMethodClasses(pluginVersion);
            pluginVersion.setMethodClassInfoList(methodClassInfos);

            // 设置插件版本信息
            pluginInfo.setLatestVersion(pluginVersion.getVersion());
            pluginInfo.setVersionCount(1);
            if (pluginInfo.getPluginVersionList() == null || pluginInfo.getPluginVersionList().isEmpty()) {
                pluginInfo.setPluginVersionList(java.util.Collections.singletonList(pluginVersion));
            }

            log.info("插件分析完成: {}", pluginInfo.getName());
            return pluginInfo;

        } finally {
            // 所有资源已通过try-with-resources关闭
        }
    }

    // 保留原有的test方法用于测试
    @SneakyThrows
    @Override
    public PluginInfo test() {
        String jarPath = "D:/GeneralBot/SDK/plugin-sdk/target/plugin-sdk-0.1.0-SNAPSHOT.jar";
        String entityPackage = "pojo";  // 用户指定的实体类包名
        String methodPackage = "test";  // 用户指定的方法包名

        PluginInfo pluginInfo = new PluginInfo();
        pluginInfo.setDescription("测试插件信息");
        pluginInfo.setAuthorId("system");
        
        // 创建并设置插件版本信息
        PluginVersion pluginVersion = new PluginVersion();
        pluginVersion.setPath(jarPath);
        pluginVersion.setEntityPackage(entityPackage);
        pluginVersion.setMethodPackage(methodPackage);
        
        pluginInfo.setPluginVersionList(java.util.Collections.singletonList(pluginVersion));

        return analyzePlugin(pluginInfo);
    }
    
    /**
     * 计算文件MD5值
     * @param file 文件
     * @return MD5值
     */
    @SneakyThrows
    private String calculateMD5(File file) {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
        try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
        }

        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}