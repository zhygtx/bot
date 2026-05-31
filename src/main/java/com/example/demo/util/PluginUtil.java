package com.example.demo.util;

import com.example.demo.pojo.plugin.*;
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

                        // 获取类的属性信息并转换为 Attribute 列表
                        Object instance = clazz.getDeclaredConstructor().newInstance();
                        Map<String, Object> attributeMap = ReflectionUtil.getAttributeMapWithType(instance);
                        
                        // 将 Map 转换为 Attribute 列表
                        List<Attribute> attributes = new ArrayList<>();
                        for (Map.Entry<String, Object> attrEntry : attributeMap.entrySet()) {
                            Attribute attribute = new Attribute();
                            attribute.setId(UUID.randomUUID().toString());
                            attribute.setEntityInfoId(entityInfo.getId());
                            attribute.setName(attrEntry.getKey());
                            // 从值中提取类型信息（假设值的格式为 "type:value" 或者直接使用值的类名）
                            Object value = attrEntry.getValue();
                            String type = value != null ? value.getClass().getSimpleName() : "Object";
                            attribute.setType(type);
                            attribute.setDescription(""); // 默认空描述
                            attributes.add(attribute);
                        }
                        entityInfo.setAttributes(attributes);

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
                
                    // 过滤掉合成类（匿名内部类、lambda 表达式等）
                    if (className.contains("$")) {
                        log.debug("跳过合成类：{}", className);
                        continue;
                    }
                
                    try {
                        Class<?> clazz = classLoader.loadClass(className);
                                        
                        // 双重检查：使用 isSynthetic() 方法过滤
                        if (clazz.isSynthetic()) {
                            log.debug("跳过合成类 (isSynthetic): {}", className);
                            continue;
                        }
                                        
                        // 只获取public方法，不包括私有方法
                        Method[] methods = clazz.getMethods();

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
                            // 过滤掉合成方法（包括 lambda 方法）
                            if (method.isSynthetic()) {
                                log.debug("跳过合成方法：{}.{}", className, method.getName());
                                continue;
                            }

                            // 过滤掉从Object类继承的方法
                            if (method.getDeclaringClass() == Object.class) {
                                log.debug("跳过Object类方法：{}.{}", className, method.getName());
                                continue;
                            }

                            MethodInfo methodInfo = new MethodInfo();
                            String methodId = UUID.randomUUID().toString();
                            methodInfo.setId(methodId);
                            methodInfo.setMethodClassId(classId);  // 关联到方法类
                            methodInfo.setName(method.getName());
                            methodInfo.setReturnType(method.getReturnType().getSimpleName());

                            // 解析方法参数并创建 ParameterInfo 列表
                            Class<?>[] paramTypes = method.getParameterTypes();
                            java.lang.reflect.Parameter[] params = method.getParameters();
                            List<ParameterInfo> parameters = new ArrayList<>();
                            for (int i = 0; i < params.length; i++) {
                                ParameterInfo paramInfo = new ParameterInfo();
                                paramInfo.setId(UUID.randomUUID().toString());
                                paramInfo.setMethodId(methodId);
                                paramInfo.setName(params[i].getName());
                                paramInfo.setType(paramTypes[i].getSimpleName());
                                paramInfo.setOrder(i + 1);
                                parameters.add(paramInfo);
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
