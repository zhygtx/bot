package com.generalbot.workflow.engine;

import com.generalbot.bot.metadata.ActionMetadata;
import com.generalbot.bot.metadata.EventMetadata;
import com.generalbot.bot.metadata.FieldMetadata;
import com.generalbot.bot.metadata.ReturnFieldInfo;
import com.generalbot.bot.scanner.BotActionScanner;
import com.generalbot.bot.scanner.BotEventScanner;
import com.generalbot.plugin.entity.Attribute;
import com.generalbot.plugin.entity.EntityInfo;
import com.generalbot.plugin.entity.MethodClassInfo;
import com.generalbot.plugin.entity.MethodInfo;
import com.generalbot.plugin.entity.ParameterInfo;
import com.generalbot.plugin.entity.PluginInfo;
import com.generalbot.plugin.entity.PluginVersion;
import com.generalbot.plugin.mapper.PluginMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 统一 callable 注册表。系统事件、定时、BOT 动作和插件方法都通过 key 解析。
 */
@Slf4j
@Component
public class CallableRegistry {

    public static final String SCHEDULE_KEY = "system:schedule";

    private final BotEventScanner botEventScanner;
    private final BotActionScanner botActionScanner;
    private final PluginMapper pluginMapper;

    private final Map<String, CallableDescriptor> systemCallables = new ConcurrentHashMap<>();
    private final Map<String, CallableDescriptor> pluginCallableCache = new ConcurrentHashMap<>();
    private final Map<String, PluginInfo> pluginInfoCache = new ConcurrentHashMap<>();
    private final Map<String, URLClassLoader> classLoaderCache = new ConcurrentHashMap<>();
    private final Map<String, Method> methodCache = new ConcurrentHashMap<>();

    public CallableRegistry(BotEventScanner botEventScanner,
                            BotActionScanner botActionScanner,
                            PluginMapper pluginMapper) {
        this.botEventScanner = botEventScanner;
        this.botActionScanner = botActionScanner;
        this.pluginMapper = pluginMapper;
    }

    /**
     * 注册全部系统 callable。
     */
    @PostConstruct
    public void init() {
        for (EventMetadata event : botEventScanner.getEventMetadataList()) {
            if ("scheduledEvent".equals(event.getEventType())) {
                continue;
            }
            String key = "system:botEvent:" + event.getEventType();
            systemCallables.put(key, CallableDescriptor.builder()
                    .key(key)
                    .name(event.getEventName())
                    .description(event.getDescription())
                    .kind(CallableDescriptor.CallableKind.TRIGGER)
                    .source(CallableDescriptor.CallableSource.SYSTEM)
                    .returnType(event.getEntityInfo() != null ? event.getEntityInfo().getEntityName() : event.getEventType())
                    .parameters(List.of())
                    .returnFields(buildEventReturnFields(event))
                    .triggerType("botEvent")
                    .build());
        }

        ParameterInfo cronParam = ParameterInfo.builder()
                .name("cronExpression")
                .type("String")
                .description("Cron 表达式，最短执行间隔 5 分钟")
                .order(0)
                .nullable(false)
                .build();
        systemCallables.put(SCHEDULE_KEY, CallableDescriptor.builder()
                .key(SCHEDULE_KEY)
                .name("定时触发")
                .description("按照设定的时间间隔自动触发工作流")
                .kind(CallableDescriptor.CallableKind.TRIGGER)
                .source(CallableDescriptor.CallableSource.SYSTEM)
                .returnType("Object")
                .parameters(List.of(cronParam))
                .returnFields(List.of(
                        CallableField.builder().name("triggerTime").type("Long")
                                .description("触发时间戳").path("triggerTime").build(),
                        CallableField.builder().name("定时触发载荷").type("Object")
                                .description("整个定时触发载荷").path("").build()))
                .triggerType("schedule")
                .build());

        for (ActionMetadata action : botActionScanner.getAllActions()) {
            String key = "system:botAction:" + action.getActionName();
            String returnType = action.getReturnInfo() != null ? action.getReturnInfo().getType() : "void";
            systemCallables.put(key, CallableDescriptor.builder()
                    .key(key)
                    .name(action.getActionDisplayName())
                    .description(action.getDescription())
                    .kind(CallableDescriptor.CallableKind.TASK)
                    .source(CallableDescriptor.CallableSource.SYSTEM)
                    .returnType(returnType)
                    .parameters(action.getParameters() == null ? List.of() : action.getParameters())
                    .returnFields(buildActionReturnFields(action))
                    .triggerType("")
                    .build());
        }
        log.info("CallableRegistry 初始化完成，系统 callable 数量：{}", systemCallables.size());
    }

    /**
     * 根据 key 解析 callable 描述。
     * @param key callable key
     * @return callable 描述
     */
    public CallableDescriptor describe(String key) {
        if (key == null || key.isBlank()) {
            throw new RuntimeException("节点 callable 不能为空");
        }
        if (key.startsWith("system:")) {
            CallableDescriptor descriptor = systemCallables.get(key);
            if (descriptor == null) {
                throw new RuntimeException("未知系统节点：" + key);
            }
            return descriptor;
        }
        if (key.startsWith("plugin:")) {
            return pluginCallableCache.computeIfAbsent(key, this::loadPluginCallable);
        }
        throw new RuntimeException("未知节点类型：" + key);
    }

    /**
     * 解析插件 callable 并加载反射方法。
     * @param key callable key
     * @return 已解析的调用信息
     */
    public ResolvedCallable resolve(String key) {
        CallableDescriptor descriptor = describe(key);
        if (descriptor.getSource() == CallableDescriptor.CallableSource.SYSTEM) {
            return new ResolvedCallable(descriptor, null, null, null);
        }

        String[] parts = key.substring("plugin:".length()).split(":", 3);
        if (parts.length != 3) {
            throw new RuntimeException("插件 callable 格式错误：" + key);
        }
        PluginInfo pluginInfo = loadPluginInfo(parts[0], parts[1]);
        PluginVersion version = findVersion(pluginInfo, parts[1]);
        MethodClassInfo methodClass = findMethodClass(version, parts[2]);
        MethodInfo methodInfo = findMethod(methodClass, parts[2]);

        Class<?> clazz = loadPluginClass(version, methodClass.getClassName());
        int parameterCount = methodInfo.getParameters() == null ? 0 : methodInfo.getParameters().size();
        Method method = findMethod(clazz, methodInfo.getName(), parameterCount);
        if (method == null) {
            throw new RuntimeException("插件方法反射失败：" + methodInfo.getName());
        }
        return new ResolvedCallable(descriptor, method, version, methodClass.getClassName());
    }

    /**
     * 关闭某个插件所有版本的类加载器并清理缓存。
     * @param pluginId 插件ID
     */
    public void closePluginClassLoaders(String pluginId) {
        List<String> keysToRemove = classLoaderCache.keySet().stream()
                .filter(key -> key.startsWith(pluginId + ":"))
                .toList();
        for (String key : keysToRemove) {
            URLClassLoader loader = classLoaderCache.remove(key);
            if (loader != null) {
                try {
                    loader.close();
                } catch (Exception e) {
                    log.warn("关闭插件类加载器失败：{}", key, e);
                }
            }
        }
        pluginCallableCache.keySet().removeIf(key -> key.startsWith("plugin:" + pluginId + ":"));
        pluginInfoCache.keySet().removeIf(key -> key.startsWith(pluginId + ":"));
        methodCache.clear();
        log.info("已清理插件 {} 的类加载器与 callable 缓存", pluginId);
    }

    /**
     * 创建或获取插件版本类加载器。
     * @param version 插件版本
     * @return 类加载器
     */
    public URLClassLoader getClassLoader(PluginVersion version) {
        String cacheKey = version.getPluginId() + ":" + version.getVersion();
        return classLoaderCache.computeIfAbsent(cacheKey, key -> {
            try {
                URL jarUrl = new File(version.getPath()).toURI().toURL();
                return new URLClassLoader(new URL[]{jarUrl}, Thread.currentThread().getContextClassLoader());
            } catch (Exception e) {
                throw new RuntimeException("创建插件类加载器失败：" + e.getMessage(), e);
            }
        });
    }

    private CallableDescriptor loadPluginCallable(String key) {
        String[] parts = key.substring("plugin:".length()).split(":", 3);
        if (parts.length != 3) {
            throw new RuntimeException("插件 callable 格式错误：" + key);
        }
        PluginInfo pluginInfo = loadPluginInfo(parts[0], parts[1]);
        PluginVersion version = findVersion(pluginInfo, parts[1]);
        MethodClassInfo methodClass = findMethodClass(version, parts[2]);
        MethodInfo methodInfo = findMethod(methodClass, parts[2]);

        List<ParameterInfo> parameters = methodInfo.getParameters() == null
                ? List.of()
                : methodInfo.getParameters().stream()
                        .sorted(Comparator.comparingInt(p -> p.getOrder() == null ? 0 : p.getOrder()))
                        .toList();
        return CallableDescriptor.builder()
                .key(key)
                .name(methodInfo.getName())
                .description(methodInfo.getDescription())
                .kind(CallableDescriptor.CallableKind.TASK)
                .source(CallableDescriptor.CallableSource.PLUGIN)
                .returnType(methodInfo.getReturnType())
                .parameters(parameters)
                .returnFields(buildPluginReturnFields(version, methodInfo))
                .triggerType("")
                .build();
    }

    /**
     * 构建 BOT 事件的可映射字段：整个事件对象 + 事件实体字段。
     */
    private List<CallableField> buildEventReturnFields(EventMetadata event) {
        String returnType = event.getEntityInfo() != null ? event.getEntityInfo().getEntityName() : event.getEventType();
        List<CallableField> fields = new java.util.ArrayList<>();
        fields.add(wholeReturnField("整个事件对象", returnType));
        if (event.getEntityInfo() != null && event.getEntityInfo().getFields() != null) {
            for (FieldMetadata field : event.getEntityInfo().getFields()) {
                fields.add(CallableField.builder()
                        .name(field.getFieldName())
                        .type(field.getFieldType())
                        .description(field.getDescription())
                        .path(field.getFieldName())
                        .build());
            }
        }
        return fields;
    }

    /**
     * 构建 BOT 动作的可映射字段；简单返回值的 value 字段归一化为整个返回值。
     */
    private List<CallableField> buildActionReturnFields(ActionMetadata action) {
        if (action.getReturnInfo() == null) {
            return List.of(wholeReturnField("返回值", "void"));
        }
        List<CallableField> fields = new java.util.ArrayList<>();
        fields.add(wholeReturnField("返回值", action.getReturnInfo().getType()));
        if (action.getReturnInfo().getFields() != null) {
            for (ReturnFieldInfo field : action.getReturnInfo().getFields()) {
                String path = "value".equals(field.getFieldPath()) ? "" : field.getFieldPath();
                fields.add(CallableField.builder()
                        .name(field.getName())
                        .type(field.getType())
                        .description(field.getDescription())
                        .path(path)
                        .build());
            }
        }
        return fields;
    }

    /**
     * 构建插件方法的可映射字段：能匹配到实体类时平铺属性，否则只有整个返回值。
     */
    private List<CallableField> buildPluginReturnFields(PluginVersion version, MethodInfo methodInfo) {
        String returnType = methodInfo.getReturnType();
        List<CallableField> fields = new java.util.ArrayList<>();
        fields.add(wholeReturnField("返回值", returnType));
        if (version.getEntityInfoList() == null) {
            return fields;
        }
        for (EntityInfo entity : version.getEntityInfoList()) {
            boolean matched = returnType.equals(entity.getEntityName())
                    || returnType.equals(entity.getName())
                    || returnType.equals(simpleName(entity.getEntityName()));
            if (matched && entity.getAttributes() != null) {
                for (Attribute attribute : entity.getAttributes()) {
                    fields.add(CallableField.builder()
                            .name(attribute.getName())
                            .type(attribute.getType())
                            .description(attribute.getDescription())
                            .path(attribute.getName())
                            .build());
                }
                break;
            }
        }
        return fields;
    }

    /**
     * 构建“整个返回值”字段，path 为空串。
     */
    private CallableField wholeReturnField(String name, String type) {
        return CallableField.builder().name(name).type(type).description("整个返回值").path("").build();
    }

    /**
     * 取全限定类名的简单名。
     */
    private String simpleName(String className) {
        if (className == null) {
            return "";
        }
        int dot = className.lastIndexOf('.');
        return dot >= 0 ? className.substring(dot + 1) : className;
    }

    private PluginInfo loadPluginInfo(String pluginId, String versionId) {
        String cacheKey = pluginId + ":" + versionId;
        return pluginInfoCache.computeIfAbsent(cacheKey, key -> {
            PluginInfo pluginInfo = pluginMapper.selectByPluginIdAndVersionId(pluginId, versionId);
            if (pluginInfo == null) {
                throw new RuntimeException("插件不存在：" + pluginId + ":" + versionId);
            }
            return pluginInfo;
        });
    }

    private PluginVersion findVersion(PluginInfo pluginInfo, String versionId) {
        return pluginInfo.getPluginVersionList().stream()
                .filter(version -> versionId.equals(version.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("插件版本不存在：" + versionId));
    }

    private MethodClassInfo findMethodClass(PluginVersion version, String methodId) {
        return version.getMethodClassInfoList().stream()
                .filter(mc -> mc.getMethods().stream().anyMatch(m -> methodId.equals(m.getId())))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("插件方法不存在：" + methodId));
    }

    private MethodInfo findMethod(MethodClassInfo methodClass, String methodId) {
        return methodClass.getMethods().stream()
                .filter(m -> methodId.equals(m.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("插件方法不存在：" + methodId));
    }

    private Class<?> loadPluginClass(PluginVersion version, String className) {
        try {
            return Class.forName(className, true, getClassLoader(version));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("加载插件类失败：" + className, e);
        }
    }

    /**
     * 查找方法（带缓存）。
     * @param clazz 类
     * @param methodName 方法名
     * @param parameterCount 参数数量
     * @return 方法
     */
    public Method findMethod(Class<?> clazz, String methodName, int parameterCount) {
        String cacheKey = clazz.getName() + ":" + methodName + ":" + parameterCount
                + ":" + clazz.getClassLoader().hashCode();
        return methodCache.computeIfAbsent(cacheKey, key -> {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equals(methodName) && method.getParameterCount() == parameterCount) {
                    return method;
                }
            }
            Class<?> superClass = clazz.getSuperclass();
            while (superClass != null && superClass != Object.class) {
                for (Method method : superClass.getDeclaredMethods()) {
                    if (method.getName().equals(methodName) && method.getParameterCount() == parameterCount) {
                        return method;
                    }
                }
                superClass = superClass.getSuperclass();
            }
            return null;
        });
    }

    /**
     * 已解析的调用信息。
     * @param descriptor callable 描述
     * @param method 反射方法
     * @param pluginVersion 插件版本（系统 callable 为 null）
     * @param className 插件类名（系统 callable 为 null）
     */
    public record ResolvedCallable(CallableDescriptor descriptor,
                                   Method method,
                                   PluginVersion pluginVersion,
                                   String className) {
    }
}
