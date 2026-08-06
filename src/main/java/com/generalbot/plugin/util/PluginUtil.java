package com.generalbot.plugin.util;

import com.generalbot.plugin.entity.*;
import com.github.zhygtx.annotation.Attribute;
import com.github.zhygtx.annotation.Entity;
import com.github.zhygtx.annotation.MethodClass;
import com.github.zhygtx.annotation.Param;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Slf4j
@Component
public class PluginUtil {

    @SneakyThrows
    public List<EntityInfo> scanEntities(PluginVersion pluginVersion) {
        String jarPath = pluginVersion.getPath();
        File file = new File(jarPath);
        List<EntityInfo> entityInfos = new ArrayList<>();

        try (JarFile jarFile = new JarFile(jarPath);
             URLClassLoader classLoader = new URLClassLoader(
                 new URL[]{file.toURI().toURL()},
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
                        EntityInfo entityInfo = new EntityInfo();
                        entityInfo.setId(UUID.randomUUID().toString());
                        entityInfo.setPluginVersionId(pluginVersion.getId());
                        entityInfo.setEntityName(className);
                        entityInfo.setName(clazz.getSimpleName());

                        Entity entityAnnotation =
                            clazz.getAnnotation(Entity.class);
                        if (entityAnnotation != null) {
                            entityInfo.setDescription(entityAnnotation.description());
                        }

                        Object instance = clazz.getDeclaredConstructor().newInstance();
                        Map<String, Object> attributeMap = ReflectionUtil.getAttributeMapWithType(instance);

                        List<com.generalbot.plugin.entity.Attribute> attributes = new ArrayList<>();
                        for (Map.Entry<String, Object> attrEntry : attributeMap.entrySet()) {
                            com.generalbot.plugin.entity.Attribute attribute =
                                new com.generalbot.plugin.entity.Attribute();
                            attribute.setId(UUID.randomUUID().toString());
                            attribute.setEntityInfoId(entityInfo.getId());
                            attribute.setName(attrEntry.getKey());
                            Object value = attrEntry.getValue();
                            String type = value != null ? value.getClass().getSimpleName() : "Object";
                            attribute.setType(type);

                            try {
                                Field field = clazz.getDeclaredField(attrEntry.getKey());
                                Attribute attrAnnotation =
                                    field.getAnnotation(Attribute.class);
                                if (attrAnnotation != null) {
                                    attribute.setDescription(attrAnnotation.description());
                                }
                            } catch (NoSuchFieldException e) {
                                log.debug("Cannot find field: {}", attrEntry.getKey());
                            }

                            attributes.add(attribute);
                        }
                        entityInfo.setAttributes(attributes);
                        entityInfos.add(entityInfo);
                        log.info("Found entity: {}", className);
                    } catch (Exception e) {
                        log.warn("Cannot load class {}: {}", className, e.getMessage());
                    }
                }
            }
        }
        return entityInfos;
    }

    @SneakyThrows
    public List<MethodClassInfo> scanMethodClasses(PluginVersion pluginVersion) {
        String jarPath = pluginVersion.getPath();
        File file = new File(jarPath);
        Map<String, MethodClassInfo> classInfoMap = new HashMap<>();
        Map<String, List<MethodInfo>> methodMap = new HashMap<>();

        try (JarFile jarFile = new JarFile(jarPath);
             URLClassLoader classLoader = new URLClassLoader(
                 new URL[]{file.toURI().toURL()},
                 Thread.currentThread().getContextClassLoader())) {

            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                if (entry.getName().contains(pluginVersion.getMethodPackage().replace(".", "/")) &&
                    entry.getName().endsWith(".class")) {

                    String className = entry.getName()
                        .replace("/", ".")
                        .substring(0, entry.getName().length() - 6);

                    if (className.contains("$")) {
                        log.debug("Skip synthetic class: {}", className);
                        continue;
                    }

                    try {
                        Class<?> clazz = classLoader.loadClass(className);

                        if (clazz.isSynthetic()) {
                            log.debug("Skip synthetic class (isSynthetic): {}", className);
                            continue;
                        }

                        Method[] methods = clazz.getMethods();
                        MethodClassInfo classInfo = new MethodClassInfo();
                        String classId = UUID.randomUUID().toString();
                        classInfo.setId(classId);
                        classInfo.setPluginVersionId(pluginVersion.getId());
                        classInfo.setClassName(className);
                        classInfo.setSimpleClassName(clazz.getSimpleName());
                        classInfo.setPackageName(clazz.getPackage().getName());

                        MethodClass serviceAnnotation =
                            clazz.getAnnotation(MethodClass.class);
                        if (serviceAnnotation != null) {
                            classInfo.setDescription(serviceAnnotation.description());
                        }

                        List<MethodInfo> classMethods = new ArrayList<>();

                        for (Method method : methods) {
                            if (method.isSynthetic()) {
                                log.debug("Skip synthetic method: {}.{}", className, method.getName());
                                continue;
                            }

                            if (method.getDeclaringClass() == Object.class) {
                                log.debug("Skip Object class method: {}.{}", className, method.getName());
                                continue;
                            }

                            MethodInfo methodInfo = new MethodInfo();
                            String methodId = UUID.randomUUID().toString();
                            methodInfo.setId(methodId);
                            methodInfo.setMethodClassId(classId);
                            methodInfo.setName(method.getName());
                            methodInfo.setReturnType(method.getReturnType().getSimpleName());

                            com.github.zhygtx.annotation.Method methodAnnotation =
                                method.getAnnotation(com.github.zhygtx.annotation.Method.class);
                            if (methodAnnotation != null) {
                                methodInfo.setDescription(methodAnnotation.description());
                                methodInfo.setReturnDescription(methodAnnotation.returnDescription());
                            }

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

                                Param paramAnnotation =
                                    params[i].getAnnotation(Param.class);
                                if (paramAnnotation != null) {
                                    paramInfo.setDescription(paramAnnotation.description());
                                    paramInfo.setNullable(paramAnnotation.nullable());
                                }

                                parameters.add(paramInfo);
                            }
                            methodInfo.setParameters(parameters);
                            classMethods.add(methodInfo);
                            methodMap.computeIfAbsent(classId, k -> new ArrayList<>()).add(methodInfo);
                            log.info("Found method: {}.{}", className, method.getName());
                        }

                        classInfo.setMethods(classMethods);
                        classInfoMap.put(classId, classInfo);
                    } catch (Exception e) {
                        log.warn("Cannot analyze class {}: {}", className, e.getMessage());
                    }
                }
            }
        }
        return new ArrayList<>(classInfoMap.values());
    }
}
