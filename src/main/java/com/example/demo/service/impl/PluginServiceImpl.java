package com.example.demo.service.impl;

import com.example.demo.pojo.plugin.EntityInfo;
import com.example.demo.pojo.plugin.MethodClassInfo;
import com.example.demo.pojo.plugin.PluginInfo;
import com.example.demo.service.PluginService;
import com.example.demo.util.PluginUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.jar.JarFile;

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
        // 创建JarFile对象，用于读取JAR文件内容
        JarFile jarFile = new JarFile(pluginInfo.getPath());
        File file = new File(pluginInfo.getPath());

        // 创建ClassLoader用于加载类
        URL jarUrl = file.toURI().toURL();
        URLClassLoader classLoader = new URLClassLoader(new URL[]{jarUrl},
                Thread.currentThread().getContextClassLoader());

        try {
            // 初始化PluginInfo对象
            pluginInfo.setId(UUID.randomUUID().toString());
            pluginInfo.setName(pluginUtil.extractPluginName(pluginInfo.getPath()));
            pluginInfo.setCreateTime(LocalDateTime.now());
            pluginInfo.setUpdateTime(LocalDateTime.now());
            pluginInfo.setIsPublic(true);
            pluginInfo.setFileSize(file.length());
            pluginInfo.setFileMd5(pluginUtil.calculateMD5(file));

            // 扫描实体类信息
            List<EntityInfo> entityInfos = pluginUtil.scanEntities(jarFile, classLoader, pluginInfo.getEntityPackage(), pluginInfo.getId());
            pluginInfo.setEntityInfoList(entityInfos);

            // 扫描方法类信息
            List<MethodClassInfo> methodClassInfos = pluginUtil.scanMethodClasses(jarFile, classLoader, pluginInfo.getMethodPackage(), pluginInfo.getId());
            pluginInfo.setMethodClassInfoList(methodClassInfos);

            // 设置方法类信息列表
            pluginInfo.setMethodClassInfoList(methodClassInfos);

            log.info("插件分析完成: {}", pluginInfo.getName());
            return pluginInfo;

        } finally {
            jarFile.close();
            classLoader.close();
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
        pluginInfo.setVersion("1.0.0");
        pluginInfo.setDescription("测试插件信息");
        pluginInfo.setAuthorId("system");
        pluginInfo.setPath(jarPath);
        pluginInfo.setEntityPackage(entityPackage);
        pluginInfo.setMethodPackage(methodPackage);

        return analyzePlugin(pluginInfo);
    }
}