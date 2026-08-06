package com.generalbot.bot.scanner;

import com.generalbot.bot.annotation.ActionParam;
import com.generalbot.bot.annotation.BotAction;
import com.generalbot.bot.action.BotActionService;
import com.generalbot.bot.metadata.ActionMetadata;
import com.generalbot.bot.metadata.ActionReturnInfo;
import com.generalbot.bot.metadata.ReturnFieldInfo;
import com.generalbot.plugin.entity.ParameterFieldInfo;
import com.generalbot.plugin.entity.ParameterInfo;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

/**
 * BOT 动作元数据扫描器。
 * <p>
 * 扫描 {@link BotActionService} 中标注了 {@link BotAction} 的方法，
 * 提取参数元数据（含复杂类型平铺）和返回值元数据。
 */
@Component
@Slf4j
public class BotActionScanner {

    private final List<ActionMetadata> actionMetadataList = new ArrayList<>();

    @PostConstruct
    public void init() {
        scanActions();
        log.info("BOT动作扫描完成: {}个动作", actionMetadataList.size());
    }

    // ==================== 主扫描逻辑 ====================

    /**
     * 扫描 BotActionService 中所有带 @BotAction 的方法。
     */
    private void scanActions() {
        try {
            Class<BotActionService> serviceClass = BotActionService.class;

            for (Method method : serviceClass.getDeclaredMethods()) {
                BotAction botAction = method.getAnnotation(BotAction.class);
                if (botAction != null) {
                    ActionMetadata metadata = extractActionMetadata(method, botAction);
                    actionMetadataList.add(metadata);
                    log.info("扫描到BOT动作: {} ({})", botAction.name(), method.getName());
                }
            }

            actionMetadataList.sort(Comparator.comparingInt(ActionMetadata::getOrder));
        } catch (Exception e) {
            log.error("扫描BOT动作失败", e);
        }
    }

    /**
     * 提取单个方法的完整 ActionMetadata。
     */
    private ActionMetadata extractActionMetadata(Method method, BotAction botAction) {
        List<ParameterInfo> parameters = extractParameters(method);
        ActionReturnInfo returnInfo = extractReturnInfo(method, botAction);

        return ActionMetadata.builder()
                .actionName(method.getName())
                .actionDisplayName(botAction.name())
                .description(botAction.description())
                .order(botAction.order())
                .categories(Arrays.asList(botAction.categories()))
                .categoryOrders(Arrays.stream(botAction.categoryOrders()).boxed().collect(Collectors.toList()))
                .parameters(parameters)
                .returnInfo(returnInfo)
                .build();
    }

    // ==================== 参数提取 ====================

    /**
     * 提取方法的所有参数元数据。
     * 如果参数类型为复杂 POJO，同时平铺其字段到 children。
     */
    private List<ParameterInfo> extractParameters(Method method) {
        List<ParameterInfo> parameters = new ArrayList<>();
        Parameter[] methodParams = method.getParameters();

        for (int i = 0; i < methodParams.length; i++) {
            Parameter param = methodParams[i];
            ActionParam actionParam = param.getAnnotation(ActionParam.class);

            String description = actionParam != null ? actionParam.description() : "参数";
            int order = i;
            boolean nullable = actionParam != null && actionParam.nullable();
            String typeName = FieldScanUtil.getSimpleTypeName(param.getType());

            ParameterInfo paramInfo = ParameterInfo.builder()
                    .id(String.valueOf(i + 1))
                    .name(param.getName())
                    .type(typeName)
                    .description(description)
                    .order(order)
                    .nullable(nullable)
                    .build();

            // 复杂类型 → 平铺字段
            if (!FieldScanUtil.isSimpleType(param.getType())) {
                paramInfo.setChildren(flattenComplexType(param.getType(), param.getName()));
            }

            parameters.add(paramInfo);
        }

        return parameters;
    }

    /**
     * 平铺复杂类型的所有字段。
     * 对于嵌套对象字段，递归平铺。
     */
    private List<ParameterFieldInfo> flattenComplexType(Class<?> type, String paramName) {
        return flattenFields(type, paramName, paramName, new HashSet<>());
    }

    private List<ParameterFieldInfo> flattenFields(
            Class<?> type, String fieldPathPrefix, String paramName, Set<String> visited) {

        List<ParameterFieldInfo> result = new ArrayList<>();

        // 收集继承链字段
        List<Field> allFields = collectFields(type);

        int order = 0;
        for (Field field : allFields) {
            if (FieldScanUtil.isScannableField(field)) continue;

            String jsonName = FieldScanUtil.getJsonFieldName(field);
            String fullPath = fieldPathPrefix + "." + jsonName;
            String desc = FieldScanUtil.resolveFieldDescription(type, field);

            ParameterFieldInfo pfi = ParameterFieldInfo.builder()
                    .name(jsonName)
                    .type(FieldScanUtil.getSimpleTypeName(field.getType()))
                    .description(desc)
                    .fieldPath(fullPath)
                    .required(false)
                    .order(order++)
                    .build();

            result.add(pfi);

            // 递归平铺嵌套对象（避免循环引用）
            if (!FieldScanUtil.isSimpleType(field.getType())) {
                String nestedKey = field.getType().getName();
                if (visited.add(nestedKey)) {
                    result.addAll(flattenFields(
                            field.getType(), fullPath, paramName, visited));
                }
            }
        }

        return result;
    }

    /**
     * 收集一个类的所有声明字段（含父类，子类覆盖父类）。
     */
    private List<Field> collectFields(Class<?> clazz) {
        Map<String, Field> fieldMap = new LinkedHashMap<>();
        List<Class<?>> hierarchy = new ArrayList<>();

        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            hierarchy.add(0, current);
            current = current.getSuperclass();
        }

        for (Class<?> c : hierarchy) {
            for (Field f : c.getDeclaredFields()) {
                String jsonName = FieldScanUtil.getJsonFieldName(f);
                fieldMap.put(jsonName, f);
            }
        }

        return new ArrayList<>(fieldMap.values());
    }

    // ==================== 返回值分析 ====================

    /**
     * 提取方法的返回值元数据。
     * <p>
     * 针对不同返回值类型：
     * <ul>
     *   <li>{@code void} — 无返回值，忽略 returnDescription</li>
     *   <li>基本类型 / String — 包装为单个 "value" 字段，使用 returnDescription</li>
     *   <li>集合类型 {@code List<T>/Set<T>/Collection<T>} — 尝试提取泛型参数 T，
     *       简单 T 包装为 "value"，复杂 T 平铺其字段</li>
     *   <li>复杂 POJO — 平铺所有字段，returnDescription 作为整体描述兜底</li>
     * </ul>
     */
    private ActionReturnInfo extractReturnInfo(Method method, BotAction botAction) {
        Class<?> returnType = method.getReturnType();
        String returnDesc = botAction.returnDescription();

        // void 类型
        if (returnType == void.class || returnType == Void.class) {
            return ActionReturnInfo.builder()
                    .type("void")
                    .description("无返回值")
                    .fields(List.of())
                    .build();
        }

        // 集合类型 → 解析泛型参数
        if (Collection.class.isAssignableFrom(returnType)) {
            Class<?> elementType = resolveGenericElementType(method);
            if (elementType != null) {
                if (FieldScanUtil.isSimpleType(elementType)) {
                    // List<String> / List<Long> → 简单类型
                    String desc = !returnDesc.isEmpty() ? returnDesc : elementType.getSimpleName() + "列表";
                    return ActionReturnInfo.builder()
                            .type("List<" + elementType.getSimpleName() + ">")
                            .description(desc)
                            .fields(List.of(
                                    ReturnFieldInfo.builder()
                                            .name("value")
                                            .type(elementType.getSimpleName())
                                            .description(desc)
                                            .fieldPath("value")
                                            .inherited(false)
                                            .order(0)
                                            .build()
                            ))
                            .build();
                } else {
                    // List<AiCharactersData> → 使用元素类型扫描字段
                    List<ReturnFieldInfo> fields = flattenReturnFields(elementType);
                    String desc = !returnDesc.isEmpty() ? returnDesc : elementType.getSimpleName();
                    return ActionReturnInfo.builder()
                            .type(elementType.getSimpleName())
                            .description(desc)
                            .fields(fields)
                            .build();
                }
            }
            // 无法解析泛型，当作 Object
        }

        // 简单类型 → 包装为 "value"
        if (FieldScanUtil.isSimpleType(returnType)) {
            String desc = !returnDesc.isEmpty() ? returnDesc : "返回值";
            return ActionReturnInfo.builder()
                    .type(returnType.getSimpleName())
                    .description(desc)
                    .fields(List.of(
                            ReturnFieldInfo.builder()
                                    .name("value")
                                    .type(returnType.getSimpleName())
                                    .description(desc)
                                    .fieldPath("value")
                                    .inherited(false)
                                    .order(0)
                                    .build()
                    ))
                    .build();
        }

        // 复杂 POJO → 平铺字段
        List<ReturnFieldInfo> fields = flattenReturnFields(returnType);
        String desc = !returnDesc.isEmpty() ? returnDesc : "返回值";
        return ActionReturnInfo.builder()
                .type(returnType.getSimpleName())
                .description(desc)
                .fields(fields)
                .build();
    }

    /**
     * 从方法的泛型返回类型中提取集合的元素类型。
     * <p>
     * 例如 {@code List<AiCharactersData>} 返回 {@code AiCharactersData.class}；
     * 无法解析时返回 {@code null}。
     */
    private Class<?> resolveGenericElementType(Method method) {
        java.lang.reflect.Type genericReturnType = method.getGenericReturnType();
        if (genericReturnType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genericReturnType;
            java.lang.reflect.Type[] typeArgs = pt.getActualTypeArguments();
            if (typeArgs.length > 0 && typeArgs[0] instanceof Class) {
                return (Class<?>) typeArgs[0];
            }
        }
        return null;
    }

    /**
     * 平铺返回值类型的所有字段。
     * 逻辑与事件字段扫描类似，沿继承链向下，子类覆盖父类。
     */
    private List<ReturnFieldInfo> flattenReturnFields(Class<?> returnType) {
        Map<String, ReturnFieldInfo> fieldMap = new LinkedHashMap<>();

        // 收集继承链（父类在前）
        List<Class<?>> hierarchy = new ArrayList<>();
        Class<?> current = returnType;
        while (current != null && current != Object.class) {
            hierarchy.add(0, current);
            current = current.getSuperclass();
        }

        int order = 0;
        for (Class<?> clazz : hierarchy) {
            boolean isInherited = clazz != returnType;

            for (Field field : clazz.getDeclaredFields()) {
                if (FieldScanUtil.isScannableField(field)) continue;

                String jsonName = FieldScanUtil.getJsonFieldName(field);
                String desc = FieldScanUtil.resolveFieldDescription(clazz, field);

                ReturnFieldInfo rfi = ReturnFieldInfo.builder()
                        .name(jsonName)
                        .type(FieldScanUtil.getSimpleTypeName(field.getType()))
                        .description(desc)
                        .fieldPath(jsonName)
                        .inherited(isInherited)
                        .order(order++)
                        .build();

                fieldMap.put(jsonName, rfi);
            }
        }

        return new ArrayList<>(fieldMap.values());
    }

    // ==================== 公开查询方法 ====================

    /** 获取所有动作元数据（只读） */
    public List<ActionMetadata> getAllActions() {
        return Collections.unmodifiableList(actionMetadataList);
    }

    /** 获取动作方法的参数数量 */
    public int getMethodParamCount(String methodName) {
        for (ActionMetadata metadata : actionMetadataList) {
            if (metadata.getActionName().equals(methodName)) {
                return metadata.getParameters().size();
            }
        }
        return 0;
    }

    /** 获取动作方法参数的索引 */
    public int getMethodParamIndex(String methodName, String paramName) {
        for (ActionMetadata metadata : actionMetadataList) {
            if (metadata.getActionName().equals(methodName)) {
                List<ParameterInfo> params = metadata.getParameters();
                for (int i = 0; i < params.size(); i++) {
                    if (params.get(i).getName().equals(paramName)) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }
}
