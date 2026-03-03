package com.example.demo.service.impl;

import com.example.demo.mapper.plugin.*;
import com.example.demo.pojo.Result;
import com.example.demo.pojo.plugin.*;
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
    private final EntityInfoMapper entityInfoMapper;
    private final MethodInfoMapper methodInfoMapper;
    private final MethodClassInfoMapper methodClassInfoMapper;
    private final ParameterInfoMapper parameterInfoMapper;
    private final PluginVersionMapper pluginVersionMapper;

    public PluginServiceImpl(PluginUtil pluginUtil, PluginMapper pluginMapper, EntityInfoMapper entityInfoMapper, MethodInfoMapper methodInfoMapper, MethodClassInfoMapper methodClassInfoMapper, ParameterInfoMapper parameterInfoMapper, PluginVersionMapper pluginVersionMapper) {
        this.pluginUtil = pluginUtil;
        this.pluginMapper = pluginMapper;
        this.entityInfoMapper = entityInfoMapper;
        this.methodInfoMapper = methodInfoMapper;
        this.methodClassInfoMapper = methodClassInfoMapper;
        this.parameterInfoMapper = parameterInfoMapper;
        this.pluginVersionMapper = pluginVersionMapper;
    }

    /**
     * 添加插件
     * @param pluginInfo 插件信息
     * @param file 插件文件
     * @return 添加结果
     */
    @Override
    @Transactional
    public Result<?> add(PluginInfo pluginInfo, MultipartFile file) {
        //初始化插件版本信息
        PluginVersion pluginVersion = pluginInfo.getPluginVersionList().get(0);
        //初始化插件信息
        if (pluginInfo.getId() == null){
            pluginInfo.setId(UUID.randomUUID().toString());
        }
        pluginInfo.setLatestVersion(pluginVersion.getVersion());
        pluginInfo.setVersionCount(pluginInfo.getVersionCount()+1);
        pluginInfo.setCreateTime(LocalDateTime.now());
        pluginInfo.setUpdateTime(LocalDateTime.now());

        //创建存储路径
        String authorPath = pluginPath + File.separator + pluginInfo.getAuthorId();
        String pluginDirPath = authorPath + File.separator + pluginInfo.getName();

        // 创建目录
        File authorDir = new File(authorPath);
        File pluginDir = new File(pluginDirPath);

        if (!authorDir.exists()) {
            if (!authorDir.mkdirs()) {
                log.error("创建插件作者目录失败: {}", authorPath);
                return Result.error(500, "创建作者目录失败");
            }
        }

        if (!pluginDir.exists()) {
            if (!pluginDir.mkdirs()) {
                log.error("创建插件目录失败: {}", pluginDirPath);
                return Result.error(500, "创建插件目录失败");
            }
        }

        //保存文件 - 修正文件路径
        String jarName = UUID.randomUUID() + ".jar";
        String filePath = pluginDirPath + File.separator + jarName;
        File jarFile = new File(filePath);

        try {
            // 确保父目录存在
            File parentDir = jarFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                // 修复：检查 mkdirs() 的返回值
                if (!parentDir.mkdirs()) {
                    log.error("创建文件父目录失败: {}", parentDir.getAbsolutePath());
                    return Result.error(500, "创建文件目录失败");
                }
            }

            // 先保存文件，再计算MD5
            file.transferTo(jarFile);

            // 从保存的文件计算MD5
            String fileMd5 = MD5Util.calculateFileMD5(jarFile);

            //初始化插件版本信息并保存
            pluginVersion.setId(UUID.randomUUID().toString());
            pluginVersion.setPluginId(pluginInfo.getId());
            pluginVersion.setPath(filePath);
            pluginVersion.setFileSize(file.getSize());
            pluginVersion.setFileMd5(fileMd5);
            pluginVersion.setCreateTime(LocalDateTime.now());

            //扫描实体类与方法信息
            List<EntityInfo> entityInfoList = pluginUtil.scanEntities(pluginVersion);
            List<MethodClassInfo> methodClassInfoList = pluginUtil.scanMethodClasses(pluginVersion);
            pluginVersion.setEntityInfoList(entityInfoList);
            pluginVersion.setMethodClassInfoList(methodClassInfoList);

            //保存插件版本信息
            List<PluginVersion> pluginVersionList = pluginInfo.getPluginVersionList();
            pluginVersionList.set(0, pluginVersion);

            int sqlResult = 0;
            if (!pluginMapper.existsByAuthorIdAndName(pluginInfo.getAuthorId(), pluginInfo.getName())){
                sqlResult +=  pluginMapper.insert(pluginInfo);
            }else {
                sqlResult +=  pluginMapper.update(pluginInfo);
            }

            sqlResult += pluginVersionMapper.insert(pluginVersion);
            sqlResult += entityInfoMapper.insert(entityInfoList);
            sqlResult += methodClassInfoMapper.insert(methodClassInfoList);
            sqlResult += methodInfoMapper.insert(methodClassInfoList.stream()
                    .map(MethodClassInfo::getMethods)
                    .flatMap(List::stream)
                    .toList());
            sqlResult += parameterInfoMapper.insert(methodClassInfoList.stream()
                    .map(MethodClassInfo::getMethods)
                    .flatMap(List::stream)
                    .map(MethodInfo::getParameters)
                    .flatMap(List::stream).toList());

            if (sqlResult == 0) {
                // 如果数据库插入失败，删除已保存的文件
                if (jarFile.exists()) {
                    if (!jarFile.delete()){
                        log.error("删除文件失败: {}", jarFile.getAbsolutePath());
                    }
                }
                return Result.error(500, "保存插件信息失败");
            }

            return Result.success();

        } catch (Exception e) {
            log.error("插件上传处理失败: {}", filePath, e);
            // 清理可能已创建的文件
            if (jarFile.exists()) {
                if (!jarFile.delete()){
                    log.error("删除已存在文件失败: {}", jarFile.getAbsolutePath());
                }
            }
            return Result.error(500, "插件上传失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public int remove(String id){
        PluginInfo pluginInfo = pluginMapper.selectById(id);
        List<String> pluginFileList = pluginInfo.getPluginVersionList().stream()
                .map(PluginVersion::getPath)
                .toList();
        pluginFileList.forEach(filePath -> {
            log.info("删除文件: {}", filePath);
            if (!new File(filePath).delete())
                log.info("删除失败: {}", filePath);
        });
        return pluginMapper.delete(id);
    }

    /**
     * 修改插件信息
     * @param pluginInfo 插件信息
     * @return 修改结果
     */
    @Override
    @Transactional
    public int edit(PluginInfo pluginInfo){
        int sqlResult = 0;
        pluginInfo.setUpdateTime(LocalDateTime.now());
        sqlResult += pluginMapper.update(pluginInfo);
        List<PluginVersion> pluginVersionList = pluginInfo.getPluginVersionList();
        List<EntityInfo> entityInfoList = pluginVersionList.stream()
                .map(PluginVersion::getEntityInfoList)
                .flatMap(List::stream)
                .toList();
        List<MethodClassInfo> methodClassInfoList = pluginVersionList.stream()
                .map(PluginVersion::getMethodClassInfoList)
                .flatMap(List::stream)
                .toList();
        List<MethodInfo> methods = methodClassInfoList.stream()
                .map(MethodClassInfo::getMethods)
                .flatMap(List::stream)
                .toList();
        List<ParameterInfo> parameters = methods.stream()
                .map(MethodInfo::getParameters)
                .flatMap(List::stream)
                .toList();
        sqlResult +=  pluginVersionMapper.update(pluginVersionList);
        sqlResult += entityInfoMapper.update(entityInfoList);
        sqlResult += methodClassInfoMapper.update(methodClassInfoList);
        sqlResult += methodInfoMapper.update(methods);
        sqlResult += parameterInfoMapper.update(parameters);
        return sqlResult;
    }

    /**
     * 修改插件公开状态
     * @param id 插件ID
     * @param isPublic 公开状态
     * @return 修改结果
     */
    @Override
    @Transactional
    public int editPublic(String id, boolean isPublic){
        return pluginMapper.updatePublic(id, isPublic);
    }

    /**
     * 获取插件列表
     * @param authorId 插件作者ID
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 插件列表
     */
    @Override
    public PageInfo<PluginInfo> findByAuthorId(String authorId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<PluginInfo> pluginInfoList = pluginMapper.selectByAuthorId(authorId);
        return new PageInfo<>(pluginInfoList);
    }

    /**
     * 获取插件信息
     * @param id 插件ID
     * @return 插件信息
     */
    @Override
    public PluginInfo findById(String id) {
        return pluginMapper.selectById(id);
    }
}