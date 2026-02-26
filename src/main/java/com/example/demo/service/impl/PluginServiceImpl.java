package com.example.demo.service.impl;

import com.example.demo.mapper.PluginMapper;
import com.example.demo.pojo.Result;
import com.example.demo.pojo.plugin.EntityInfo;
import com.example.demo.pojo.plugin.MethodClassInfo;
import com.example.demo.pojo.plugin.PluginInfo;
import com.example.demo.pojo.plugin.PluginVersion;
import com.example.demo.service.PluginService;
import com.example.demo.util.MD5Util;
import com.example.demo.util.PluginUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class PluginServiceImpl implements PluginService {

    @Value("${upload.plugin-path}")
    private String pluginPath;

    private final PluginUtil pluginUtil;
    private final PluginMapper pluginMapper;

    public PluginServiceImpl(PluginUtil pluginUtil, PluginMapper pluginMapper) {
        this.pluginUtil = pluginUtil;
        this.pluginMapper = pluginMapper;
    }

    @Override
    @Transactional
    public Result<?> add(PluginInfo pluginInfo, MultipartFile file) {
        //初始化插件版本信息
        PluginVersion pluginVersion = pluginInfo.getPluginVersionList().get(0);
        //初始化插件信息
        pluginInfo.setId(UUID.randomUUID().toString());
        pluginInfo.setLatestVersion(pluginVersion.getVersion());
        pluginInfo.setVersionCount(pluginInfo.getVersionCount()+1);
        pluginInfo.setCreateTime(LocalDateTime.now());
        pluginInfo.setUpdateTime(LocalDateTime.now());

        //创建存储路径
        String authorPath = pluginPath + "/" + pluginInfo.getAuthorId();
        String jarPath = authorPath + "/" + pluginInfo.getName();

        // 创建目录
        File authorDir = new File(authorPath);
        File jarDir = new File(jarPath);
        if (!authorDir.exists()){
            if (!authorDir.mkdirs()){
                log.error("创建插件作者目录失败");
                return Result.error(500);
            }
        }
        if (!jarDir.exists()) {
            if (!jarDir.mkdirs()) {
                log.error("创建插件目录失败");
                return Result.error(500);
            }
        }

        //保存文件
        String jarName = UUID.randomUUID()+ ".jar";
        String filePath = pluginPath + "/" + jarName;
        File jarFile = new File(filePath);
        try {
            file.transferTo(jarFile);
        }catch (Exception e){
            log.error("文件保存失败", e);
            return Result.error(500);
        }

        //初始化插件版本信息并保存
        pluginVersion.setId(UUID.randomUUID().toString());
        pluginVersion.setPluginId(pluginInfo.getId());
        pluginVersion.setPath(jarPath);
        pluginVersion.setFileSize(file.getSize());
        try {
            pluginVersion.setFileMd5(MD5Util.calculateFileMD5(file));
        } catch (IOException | NoSuchAlgorithmException e) {
            log.error("计算文件MD5失败", e);
        }
        pluginVersion.setCreateTime(LocalDateTime.now());
        //扫描实体类与方法信息
        List<EntityInfo> entityInfoList = pluginUtil.scanEntities(pluginVersion);
        List<MethodClassInfo> methodClassInfoList = pluginUtil.scanMethodClasses(pluginVersion);
        pluginVersion.setEntityInfoList(entityInfoList);
        pluginVersion.setMethodClassInfoList(methodClassInfoList);

        //保存插件版本信息
        List<PluginVersion> pluginVersionList = pluginInfo.getPluginVersionList();
        pluginVersionList.set(0, pluginVersion);
        if (pluginMapper.insert(pluginInfo) != 1){
            return Result.error(500);
        }
        return Result.success();
    }

    @Override
    public PageInfo<PluginInfo> findByAuthorId(String authorId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<PluginInfo> pluginInfoList = pluginMapper.selectByAuthorId(authorId);
        return new PageInfo<>(pluginInfoList);
    }
}