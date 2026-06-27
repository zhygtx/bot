package com.example.demo.service.impl;

import com.example.demo.mapper.plugin.*;
import com.example.demo.mapper.workflow.WorkflowInfoMapper;
import com.example.demo.pojo.dto.PluginInfoDto;
import com.example.demo.pojo.entity.Result;
import com.example.demo.pojo.entity.plugin.*;
import com.example.demo.service.PluginService;
import com.example.demo.service.WorkflowCacheService;
import com.example.demo.util.MD5Util;
import com.example.demo.util.PluginUtil;
import com.example.demo.util.WorkflowUtil;
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
import java.util.Map;
import java.util.Objects;
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
    private final AttributeMapper attributeMapper;
    private final WorkflowUtil workflowUtil;
    private final WorkflowInfoMapper workflowInfoMapper;
    private final WorkflowCacheService workflowCacheService;

    public PluginServiceImpl(PluginUtil pluginUtil, PluginMapper pluginMapper, EntityInfoMapper entityInfoMapper, MethodInfoMapper methodInfoMapper, MethodClassInfoMapper methodClassInfoMapper, ParameterInfoMapper parameterInfoMapper, PluginVersionMapper pluginVersionMapper, AttributeMapper attributeMapper, WorkflowUtil workflowUtil, WorkflowInfoMapper workflowInfoMapper, WorkflowCacheService workflowCacheService) {
        this.pluginUtil = pluginUtil;
        this.pluginMapper = pluginMapper;
        this.entityInfoMapper = entityInfoMapper;
        this.methodInfoMapper = methodInfoMapper;
        this.methodClassInfoMapper = methodClassInfoMapper;
        this.parameterInfoMapper = parameterInfoMapper;
        this.pluginVersionMapper = pluginVersionMapper;
        this.attributeMapper = attributeMapper;
        this.workflowUtil = workflowUtil;
        this.workflowInfoMapper = workflowInfoMapper;
        this.workflowCacheService = workflowCacheService;
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
            List<MethodInfo> methodInfoList = methodClassInfoList.stream()
                    .map(MethodClassInfo::getMethods)
                    .flatMap(List::stream)
                    .toList();
            List<ParameterInfo> parameterInfoList = methodClassInfoList.stream()
                    .map(MethodClassInfo::getMethods)
                    .flatMap(List::stream)
                    .map(MethodInfo::getParameters)
                    .flatMap(List::stream)
                    .toList();
            
            // 收集所有 Attribute 信息
            List<Attribute> attributeList = entityInfoList.stream()
                    .map(EntityInfo::getAttributes)
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .toList();
            
            // 校验所有参数类型是否合法
            for (ParameterInfo parameter : parameterInfoList) {
                if (!parameter.isValidType()) {
                    log.error("插件参数类型不合法：{}", parameter.getInvalidTypeMessage());
                    // 删除已保存的文件
                    if (jarFile.exists()) {
                        if (!jarFile.delete()){
                            log.error("删除已存在文件失败：{}", jarFile.getAbsolutePath());
                        }
                    }
                    return Result.error(400, parameter.getInvalidTypeMessage());
                }
            }

            // 校验基本类型参数不能标记为 nullable
            for (ParameterInfo parameter : parameterInfoList) {
                if (!parameter.isValidNullable()) {
                    log.error("插件参数 nullable 配置不合法：{}", parameter.getInvalidNullableMessage());
                    if (jarFile.exists()) {
                        if (!jarFile.delete()){
                            log.error("删除已存在文件失败：{}", jarFile.getAbsolutePath());
                        }
                    }
                    return Result.error(400, parameter.getInvalidNullableMessage());
                }
            }

            pluginVersion.setEntityInfoList(entityInfoList);
            pluginVersion.setMethodClassInfoList(methodClassInfoList);

            int sqlResult = 0;
            if (!pluginMapper.existsByAuthorIdAndName(pluginInfo.getAuthorId(), pluginInfo.getName())){
                sqlResult +=  pluginMapper.insert(pluginInfo);
            }else {
                sqlResult +=  pluginMapper.update(pluginInfo);
            }

            sqlResult += pluginVersionMapper.insert(pluginVersion);
            sqlResult += entityInfoList.isEmpty() ? 0 : entityInfoMapper.insert(entityInfoList);
            sqlResult += attributeList.isEmpty() ? 0 : attributeMapper.insert(attributeList);
            sqlResult += methodClassInfoList.isEmpty() ? 0 : methodClassInfoMapper.insert(methodClassInfoList);
            sqlResult += methodInfoList.isEmpty() ? 0 : methodInfoMapper.insert(methodInfoList);
            sqlResult += parameterInfoList.isEmpty() ? 0 : parameterInfoMapper.insert(parameterInfoList);

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
            throw new RuntimeException("插件上传失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public int remove(String id){
        //0.标注工作流可用性
        workflowInfoMapper.updateAvailable(id, "部分相关插件已下架");

        // 1. 查询插件信息
        PluginInfo pluginInfo = pluginMapper.selectById(id);
            
        // 2. 关闭并清理所有相关的类加载器缓存（解决文件被占用的问题）
        workflowUtil.closeAllClassLoaderForPlugin(id);
        log.info("已清理插件 {} 的所有类加载器缓存", id);
            
        // 3. 从 Redis 中删除相关工作流缓存
        workflowCacheService.removeWorkflowsByPluginId(id);
            
        // 4. 删除所有版本的文件
        pluginInfo.getPluginVersionList().forEach(version -> {
            String filePath = version.getPath();
            File file = new File(filePath);
            if (file.exists() && file.delete()) {
                log.info("成功删除插件文件：{}", filePath);
            } else {
                log.warn("插件文件不存在或删除失败：{}", filePath);
            }
        });
            
        // 5. 删除数据库记录
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
        // 收集所有 Attribute 信息
        List<Attribute> attributeList = entityInfoList.stream()
                .map(EntityInfo::getAttributes)
                .filter(Objects::nonNull)
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
        sqlResult += entityInfoList.isEmpty() ? 0 : entityInfoMapper.update(entityInfoList);
        sqlResult += attributeList.isEmpty() ? 0 : attributeMapper.update(attributeList);
        sqlResult += methodClassInfoList.isEmpty() ? 0 : methodClassInfoMapper.update(methodClassInfoList);
        sqlResult += methods.isEmpty() ? 0 : methodInfoMapper.update(methods);
        sqlResult += parameters.isEmpty() ? 0 : parameterInfoMapper.update(parameters);
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
     * 根据插件ID和版本ID查询插件信息
     * @param id 插件ID
     * @param versionId 版本ID
     * @return 插件信息
     */
    @Override
    public PluginInfo findByPluginIdAndVersionId(String id, String versionId) {
        return pluginMapper.selectByPluginIdAndVersionId(id, versionId);
    }

    /**
     * 查询插件列表
     * @param content 搜索内容
     * @param authorId 创建者ID
     * @param isPublic 公开状态
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 插件列表
     */
    @Override
    public PageInfo<PluginInfoDto> findPlugins(String content, String authorId, Boolean isPublic, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return PageInfo.of(pluginMapper.selectPlugins(content, authorId, isPublic));
    }

    /**
     * 根据插件ID查询插件版本信息
     * @param pluginId 插件ID
     * @return 插件版本信息
     */
    @Override
    public List<Map<String, Object>> findPluginVersionByPluginId(String pluginId) {
        return pluginMapper.selectPluginVersionByPluginId(pluginId);
    }
}