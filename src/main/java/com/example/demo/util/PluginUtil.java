package com.example.demo.util;

import com.example.demo.pojo.plugin.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Slf4j
@Component
public class PluginUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 扫描实体类信息
     * @param pluginVersion 插件版本信息
     * @return 实体类信息列表
     */
    @SneakyThrows
    public List<EntityInfo> scanEntities(PluginVersion pluginVersion) {

        // 获取插件路径
        String jarPath = pluginVersion.getPath();
        File file = new File(jarPath);

        List<EntityInfo> entityInfos = new ArrayList<>();

        // 使用 try-with-resources 确保 JarFile 和 URLClassLoader 正确关闭
        try (JarFile jarFile = new JarFile(jarPath);
             URLClassLoader classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()},
                     Thread.currentThread().getContextClassLoader())) {

            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                if (entry.getName().contains(pluginVersion.getEntityPackage().replace(".", "/")) &&
                        entry.getName().endsWith(".class")) {

                    String className = entry.getName()
                            .replace("/", ".")
                            .substring(0, entry.getName().length() - 6);

                    try {
                        Class<?> clazz = classLoader.loadClass(className);

                        // 创建实体信息对象
                        EntityInfo entityInfo = new EntityInfo();
                        entityInfo.setId(UUID.randomUUID().toString());
                        entityInfo.setPluginVersionId(pluginVersion.getId());
                        entityInfo.setEntityName(className);
                        entityInfo.setName(clazz.getSimpleName());  // 设置简写名称

                        // 获取类的属性信息
                        Object instance = clazz.getDeclaredConstructor().newInstance();
                        Map<String, Object> attributeMap = ReflectionUtil.getAttributeMapWithType(instance);
                        entityInfo.setAttributes(mapper.writeValueAsString(attributeMap));

                        entityInfos.add(entityInfo);
                        log.info("发现实体类: {}", className);

                    } catch (Exception e) {
                        log.warn("无法加载类 {}: {}", className, e.getMessage());
                    }
                }
            }
        }
        return entityInfos;
    }

    /**
     * 扫描方法类信息
     * @param pluginVersion 插件版本信息
     * @return 方法类信息列表
     */
    @SneakyThrows
    public List<MethodClassInfo> scanMethodClasses(PluginVersion pluginVersion) {

        // 获取插件路径
        String jarPath = pluginVersion.getPath();
        File file = new File(jarPath);

        Map<String, MethodClassInfo> classInfoMap = new HashMap<>();
        Map<String, List<MethodInfo>> methodMap = new HashMap<>();

        // 使用 try-with-resources 确保 JarFile 和 URLClassLoader 正确关闭
        try (JarFile jarFile = new JarFile(jarPath);
             URLClassLoader classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()},
                     Thread.currentThread().getContextClassLoader())) {

            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                if (entry.getName().contains(pluginVersion.getMethodPackage().replace(".", "/")) &&
                        entry.getName().endsWith(".class")) {

                    String className = entry.getName()
                            .replace("/", ".")
                            .substring(0, entry.getName().length() - 6);

                    try {
                        Class<?> clazz = classLoader.loadClass(className);
                        Method[] methods = clazz.getDeclaredMethods();

                        // 创建方法类信息
                        MethodClassInfo classInfo = new MethodClassInfo();
                        String classId = UUID.randomUUID().toString();
                        classInfo.setId(classId);
                        classInfo.setPluginVersionId(pluginVersion.getId());
                        classInfo.setClassName(className);
                        classInfo.setSimpleClassName(clazz.getSimpleName());
                        classInfo.setPackageName(clazz.getPackage().getName());

                        List<MethodInfo> classMethods = new ArrayList<>();

                        // 添加该类的方法信息
                        for (Method method : methods) {
                            method.setAccessible(true);

                            MethodInfo methodInfo = new MethodInfo();
                            String methodId = UUID.randomUUID().toString();
                            methodInfo.setId(methodId);
                            methodInfo.setMethodClassId(classId);  // 关联到方法类
                            methodInfo.setName(method.getName());
                            methodInfo.setReturnType(method.getReturnType().getSimpleName());

                            // 解析方法参数并创建 ParameterInfo 列表
                            Map<String, Object> paramMap = ReflectionUtil.parseMethodSignature(method);
                            List<ParameterInfo> parameters = new ArrayList<>();
                            for (Map.Entry<String, Object> entry1 : paramMap.entrySet()) {
                                if (!"returnType".equals(entry1.getKey())) {
                                    ParameterInfo paramInfo = new ParameterInfo();
                                    paramInfo.setId(UUID.randomUUID().toString());
                                    paramInfo.setMethodId(methodId);
                                    paramInfo.setName(entry1.getKey());
                                    paramInfo.setType(entry1.getValue().toString());
                                    parameters.add(paramInfo);
                                }
                            }
                            methodInfo.setParameters(parameters);

                            classMethods.add(methodInfo);
                            methodMap.computeIfAbsent(classId, k -> new ArrayList<>()).add(methodInfo);
                            log.info("发现方法: {}.{}", className, method.getName());
                        }

                        classInfo.setMethods(classMethods);
                        classInfoMap.put(classId, classInfo);

                    } catch (Exception e) {
                        log.warn("无法分析类 {}: {}", className, e.getMessage());
                    }
                }
            }
        }

        return new ArrayList<>(classInfoMap.values());
    }

}
