package com.example.demo.util;

import com.example.demo.pojo.plugin.MethodClassInfo;
import com.example.demo.pojo.plugin.MethodInfo;
import com.example.demo.pojo.plugin.ParameterInfo;
import com.example.demo.pojo.plugin.PluginInfo;
import com.example.demo.pojo.workflow.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工作流工具类
 */
@Slf4j
@Component
public class WorkflowUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    // 缓存已加载的类加载器，避免重复加载
    private final Map<String, URLClassLoader> classLoaderCache = new ConcurrentHashMap<>();

    // 插件版本管理，用于处理插件更新
    private final Map<String, String> pluginVersionCache = new ConcurrentHashMap<>();

    /**
     * 验证工作流配置的有效性
     * @param workflowInfo 工作流信息
     * @return 验证结果
     */
    public boolean validateWorkflow(WorkflowInfo workflowInfo) {
        if (workflowInfo == null) {
            log.error("工作流信息不能为空");
            return false;
        }

        if (workflowInfo.getNodes() == null || workflowInfo.getNodes().isEmpty()) {
            log.error("工作流必须包含至少一个节点");
            return false;
        }

        log.info("工作流配置验证通过");
        return true;
    }

    /**
     * 构建工作流依赖关系图
     * @param workflowInfo 工作流信息
     * @return 工作流图
     */
    public WorkflowGraph buildWorkflowGraph(WorkflowInfo workflowInfo) {
        WorkflowGraph graph = new WorkflowGraph();
        Map<String, Node> nodeMap = new HashMap<>();

        // 构建节点映射和计算入度
        for (Node node : workflowInfo.getNodes()) {
            nodeMap.put(node.getId(), node);
            node.setInDegree(calculateInDegree(node, workflowInfo.getNodes()));
        }

        graph.setNodeMap(nodeMap);
        return graph;
    }

    /**
     * 计算节点的入度
     * @param targetNode 目标节点
     * @param allNodes 所有节点
     * @return 入度值
     */
    public int calculateInDegree(Node targetNode, List<Node> allNodes) {
        int inDegree = 0;
        for (Node node : allNodes) {
            if (node.getNextNodeId() != null && node.getNextNodeId().contains(targetNode.getId())) {
                inDegree++;
            }
        }
        return inDegree;
    }

    /**
     * 按拓扑顺序执行节点
     * @param graph 工作流图
     * @param workflowInfo 工作流信息
     * @return 执行结果
     */
    public JsonNode executeNodesInTopologicalOrder(WorkflowGraph graph, WorkflowInfo workflowInfo) {
        if (graph == null) {
            log.warn("工作流图为null，直接返回空结果");
            return mapper.createObjectNode();
        }
        
        // 确保执行上下文已初始化
        ThreadLocalManager.getExecutionContext();
        
        Map<String, Node> nodeMap = graph.getNodeMap();
        List<Set<String>> inDegreeBuckets = initializeInDegreeBuckets(nodeMap.values());
        Map<String, Integer> nodeToInDegree = new HashMap<>();

        // 初始化节点入度映射
        for (Node node : nodeMap.values()) {
            nodeToInDegree.put(node.getId(), node.getInDegree());
        }

        List<JsonNode> finalResults = new ArrayList<>();
        int processedCount = 0;

        while (true) {
            // 获取入度为0的节点
            String currentNodeId = getNextZeroInDegreeNode(inDegreeBuckets);
            if (currentNodeId == null) {
                break; // 没有更多可执行的节点
            }

            Node currentNode = nodeMap.get(currentNodeId);
            log.info("执行节点: {} (第{}个)", currentNodeId, ++processedCount);

            try {
                // 执行当前节点
                ExecutionResult executionResult = executeSingleNode(currentNode, workflowInfo);

                if (!executionResult.continueExecution()) {
                    // 结束整个工作流
                    log.info("工作流执行被条件终止");
                    return mapper.createObjectNode();
                }

                if (executionResult.result() != null) {
                    // 将结果存入上下文
                    ThreadLocalManager.getExecutionContext().put(currentNodeId, executionResult.result());

                    // 如果是最后一个节点，保存结果
                    if (currentNode.getNextNodeId() == null || currentNode.getNextNodeId().isEmpty()) {
                        finalResults.add(mapper.valueToTree(executionResult.result()));
                    }

                    // 更新后续节点的入度
                    updateSuccessorNodesInDegree(currentNode, nodeToInDegree, inDegreeBuckets);
                } else {
                    // 条件判断为BREAK，结束当前分支
                    log.info("跳过节点{}的后续分支", currentNodeId);
                }
            } catch (Exception e) {
                log.error("执行节点{}时发生错误: {}", currentNodeId, e.getMessage(), e);
                // 发生错误时结束工作流执行
                return mapper.createObjectNode();
            }
        }

        if (processedCount != nodeMap.size()) {
            log.warn("工作流执行异常：预期执行{}个节点，实际执行{}个节点",
                    nodeMap.size(), processedCount);
        }

        // 返回多结束节点的结果
        if (finalResults.isEmpty()) {
            return mapper.createObjectNode();
        } else if (finalResults.size() == 1) {
            return finalResults.getFirst();
        } else {
            // 多个结束节点，返回结果数组
            return mapper.valueToTree(finalResults);
        }
    }

    /**
     * 初始化入度桶
     * @param nodes 节点集合
     * @return 入度桶列表
     */
    public List<Set<String>> initializeInDegreeBuckets(Collection<Node> nodes) {
        int maxInDegree = nodes.stream()
                .mapToInt(Node::getInDegree)
                .max()
                .orElse(0);

        List<Set<String>> buckets = new ArrayList<>(maxInDegree + 1);
        for (int i = 0; i <= maxInDegree; i++) {
            buckets.add(new HashSet<>());
        }

        for (Node node : nodes) {
            buckets.get(node.getInDegree()).add(node.getId());
        }

        return buckets;
    }

    /**
     * 获取下一个入度为0的节点
     * @param inDegreeBuckets 入度桶
     * @return 节点ID
     */
    public String getNextZeroInDegreeNode(List<Set<String>> inDegreeBuckets) {
        Set<String> zeroDegreeNodes = inDegreeBuckets.getFirst();
        if (zeroDegreeNodes.isEmpty()) {
            return null;
        }
        String node = zeroDegreeNodes.iterator().next();
        zeroDegreeNodes.remove(node);
        return node;
    }

    /**
     * 更新后继节点的入度
     * @param currentNode 当前节点
     * @param nodeToInDegree 节点入度映射
     * @param inDegreeBuckets 入度桶
     */
    public void updateSuccessorNodesInDegree(Node currentNode,
                                           Map<String, Integer> nodeToInDegree,
                                           List<Set<String>> inDegreeBuckets) {
        if (currentNode.getNextNodeId() != null) {
            for (String nextNodeId : currentNode.getNextNodeId()) {
                int currentInDegree = nodeToInDegree.get(nextNodeId);
                if (currentInDegree > 0) {
                    // 从旧桶中移除
                    inDegreeBuckets.get(currentInDegree).remove(nextNodeId);
                    // 添加到新桶
                    inDegreeBuckets.get(currentInDegree - 1).add(nextNodeId);
                    // 更新映射
                    nodeToInDegree.put(nextNodeId, currentInDegree - 1);
                }
            }
        }
    }

    /**
     * 执行单个节点
     * @param node 节点信息
     * @param workflowInfo 工作流信息
     * @return 执行结果和是否继续执行
     * @throws Exception 执行过程中的异常
     */
    public ExecutionResult executeSingleNode(Node node, WorkflowInfo workflowInfo) throws Exception {
        // 1. 检查条件
        Condition.Action action = checkConditions(node, workflowInfo);
        if (action == Condition.Action.END) {
            log.info("条件判断结果: 结束整个工作流");
            return new ExecutionResult(null, false);
        } else if (action == Condition.Action.BREAK) {
            log.info("条件判断结果: 结束当前分支");
            return new ExecutionResult(null, true);
        }

        // 2. 获取插件信息
        PluginInfo pluginInfo = node.getPluginInfo();
        if (pluginInfo == null) {
            throw new IllegalStateException("节点" + node.getId() + "缺少插件信息");
        }

        // 3. 加载类加载器
        URLClassLoader classLoader = getClassLoader(pluginInfo);

        // 4. 获取方法信息
        MethodInfo methodInfo = node.getMethodInfo();
        MethodClassInfo methodClassInfo = node.getMethodClassInfo();

        if (methodInfo == null || methodClassInfo == null) {
            throw new IllegalStateException("节点" + node.getId() + "缺少方法信息");
        }

        // 5. 准备方法参数
        Object[] parameters = prepareMethodParameters(node, methodInfo);

        // 6. 反射调用方法
        Class<?> clazz = classLoader.loadClass(methodClassInfo.getClassName());
        Method method = findMethod(clazz, methodInfo.getName(), parameters.length);

        if (method == null) {
            throw new NoSuchMethodException("找不到方法: " + methodInfo.getName() +
                    " 在类: " + methodClassInfo.getClassName());
        }

        method.setAccessible(true);
        // 使用线程本地缓存获取或创建实例
        Object instance = getOrCreateInstance(pluginInfo, methodClassInfo, clazz);

        log.debug("调用方法: {}.{} 参数数量: {}",
                methodClassInfo.getClassName(), method.getName(), parameters.length);

        Object result = method.invoke(instance, parameters);
        return new ExecutionResult(result, true);
    }

    /**
     * 检查节点的条件
     * @param node 节点信息
     * @param workflowInfo 工作流信息
     * @return 执行动作
     */
    public Condition.Action checkConditions(Node node, WorkflowInfo workflowInfo) {
        if (workflowInfo.getConditions() == null) {
            return Condition.Action.CONTINUE;
        }

        for (Condition condition : workflowInfo.getConditions()) {
            if (condition.getNodeId().equals(node.getId())) {
                if (evaluateCondition(condition)) {
                    return condition.getAction();
                } else if (condition.getElseAction() != null) {
                    return condition.getElseAction();
                }
            }
        }
        return Condition.Action.CONTINUE;
    }

    /**
     * 评估条件
     * @param condition 条件
     * @return 是否满足条件
     */
    public boolean evaluateCondition(Condition condition) {
        Map<String, Object> context = ThreadLocalManager.getExecutionContext();
        Object fieldValue = null;

        // 获取字段值
        if ("input".equals(condition.getNodeId())) {
            Object input = context.get("input");
            if (input instanceof Map) {
                fieldValue = ((Map<?, ?>) input).get(condition.getFieldName());
            }
        } else {
            Object nodeResult = context.get(condition.getNodeId());
            if (nodeResult instanceof Map) {
                fieldValue = ((Map<?, ?>) nodeResult).get(condition.getFieldName());
            } else {
                fieldValue = nodeResult;
            }
        }

        // 评估条件
        return evaluateExpression(fieldValue, condition.getPresetContent(), condition.getOperator(), condition.getContentType());
    }

    /**
     * 评估表达式
     * @param fieldValue 字段值
     * @param presetContent 预设内容
     * @param operator 操作符
     * @param contentType 内容类型
     * @return 是否满足条件
     */
    public boolean evaluateExpression(Object fieldValue, String presetContent, Condition.Operator operator, Condition.ContentType contentType) {
        return switch (operator) {
            case IS_NULL -> fieldValue == null;
            case IS_NOT_NULL -> fieldValue != null;
            case EQUALS -> equals(fieldValue, presetContent, contentType);
            case NOT_EQUALS -> !equals(fieldValue, presetContent, contentType);
            case GREATER_THAN -> compare(fieldValue, presetContent, contentType) > 0;
            case GREATER_THAN_OR_EQUALS -> compare(fieldValue, presetContent, contentType) >= 0;
            case LESS_THAN -> compare(fieldValue, presetContent, contentType) < 0;
            case LESS_THAN_OR_EQUALS -> compare(fieldValue, presetContent, contentType) <= 0;
            case CONTAINS -> contains(fieldValue, presetContent);
            case NOT_CONTAINS -> !contains(fieldValue, presetContent);
            case REGEX -> regex(fieldValue, presetContent);
        };
    }

    /**
     * 比较两个值是否相等
     * @param fieldValue 字段值
     * @param presetContent 预设内容
     * @param contentType 内容类型
     * @return 是否相等
     */
    public boolean equals(Object fieldValue, String presetContent, Condition.ContentType contentType) {
        if (fieldValue == null && presetContent == null) {
            return true;
        }
        if (fieldValue == null || presetContent == null) {
            return false;
        }

        switch (contentType) {
            case STRING:
                return fieldValue.toString().equals(presetContent);
            case NUMBER:
                try {
                    double fieldNum = Double.parseDouble(fieldValue.toString());
                    double presetNum = Double.parseDouble(presetContent);
                    return fieldNum == presetNum;
                } catch (NumberFormatException e) {
                    return false;
                }
            case BOOLEAN:
                return Boolean.parseBoolean(fieldValue.toString()) == Boolean.parseBoolean(presetContent);
            default:
                return false;
        }
    }

    /**
     * 比较两个值
     * @param fieldValue 字段值
     * @param presetContent 预设内容
     * @param contentType 内容类型
     * @return 比较结果
     */
    public int compare(Object fieldValue, String presetContent, Condition.ContentType contentType) {
        if (fieldValue == null || presetContent == null) {
            return -1;
        }

        try {
            switch (contentType) {
                case NUMBER:
                    double fieldNum = Double.parseDouble(fieldValue.toString());
                    double presetNum = Double.parseDouble(presetContent);
                    return Double.compare(fieldNum, presetNum);
                case STRING:
                    return fieldValue.toString().compareTo(presetContent);
                default:
                    return -1;
            }
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * 检查字段值是否包含预设内容
     * @param fieldValue 字段值
     * @param presetContent 预设内容
     * @return 是否包含
     */
    public boolean contains(Object fieldValue, String presetContent) {
        if (fieldValue == null || presetContent == null) {
            return false;
        }
        return fieldValue.toString().contains(presetContent);
    }

    /**
     * 检查字段值是否匹配正则表达式
     * @param fieldValue 字段值
     * @param presetContent 预设内容
     * @return 是否匹配
     */
    public boolean regex(Object fieldValue, String presetContent) {
        if (fieldValue == null || presetContent == null) {
            return false;
        }
        return fieldValue.toString().matches(presetContent);
    }

    /**
     * 从线程本地缓存获取或创建实例
     * @param pluginInfo 插件信息
     * @param methodClassInfo 方法类信息
     * @param clazz 类对象
     * @return 实例对象
     * @throws Exception 实例创建异常
     */
    public Object getOrCreateInstance(PluginInfo pluginInfo, MethodClassInfo methodClassInfo, Class<?> clazz) throws Exception {
        // 包含插件版本的缓存键，用于处理插件更新
        String version = pluginInfo.getVersion() != null ? pluginInfo.getVersion() : "unknown";
        String cacheKey = pluginInfo.getId() + ":" + version + ":" + methodClassInfo.getClassName();
        Map<String, Object> instanceMap = ThreadLocalManager.getMethodInstanceCache();

        Object instance = instanceMap.get(cacheKey);
        if (instance == null) {
            instance = clazz.getDeclaredConstructor().newInstance();
            instanceMap.put(cacheKey, instance);
        }

        return instance;
    }

    /**
     * 获取或创建类加载器
     * @param pluginInfo 插件信息
     * @return 类加载器
     */
    public URLClassLoader getClassLoader(PluginInfo pluginInfo) {
        String pluginId = pluginInfo.getId();
        String currentVersion = pluginInfo.getVersion() != null ? pluginInfo.getVersion() : "unknown";

        // 检查插件版本是否更新
        String cachedVersion = pluginVersionCache.get(pluginId);
        if (cachedVersion == null || !cachedVersion.equals(currentVersion)) {
            // 插件版本更新，重新创建类加载器
            log.info("插件版本更新: {} 从 {} 到 {}", pluginId, cachedVersion, currentVersion);

            // 清理旧的类加载器
            classLoaderCache.remove(pluginId);

            // 更新版本缓存
            pluginVersionCache.put(pluginId, currentVersion);
        }

        return classLoaderCache.computeIfAbsent(pluginId, key -> {
            try {
                // 修复：使用URI构造URL以避免弃用警告
                URL jarUrl = new File(pluginInfo.getPath()).toURI().toURL();
                return new URLClassLoader(new URL[]{jarUrl},
                        Thread.currentThread().getContextClassLoader());
            } catch (Exception e) {
                throw new RuntimeException("创建类加载器失败: " + e.getMessage(), e);
            }
        });
    }

    /**
     * 准备方法调用参数
     * @param node 节点信息
     * @param methodInfo 方法信息
     * @return 参数数组
     */
    public Object[] prepareMethodParameters(Node node, MethodInfo methodInfo) {
        try {
            // 获取参数列表
            List<ParameterInfo> parametersList = methodInfo.getParameters();
            int parameterCount = parametersList.size();

            Object[] parameters = new Object[parameterCount];

            // 处理数据映射
            if (node.getDataMaps() != null) {
                for (DataMap dataMap : node.getDataMaps()) {
                    Object sourceValue = getSourceValue(dataMap);
                    int targetIndex = findParameterIndex(methodInfo, dataMap.getTarget());
                    if (targetIndex >= 0 && targetIndex < parameters.length) {
                        parameters[targetIndex] = convertValueType(sourceValue, dataMap.getTargetType());
                    }
                }
            }

            // 处理默认值
            if (node.getNodeDefaults() != null) {
                for (NodeDefaults nodeDefault : node.getNodeDefaults()) {
                    if (nodeDefault.getParamIndex() < parameters.length &&
                            parameters[nodeDefault.getParamIndex()] == null) {
                        parameters[nodeDefault.getParamIndex()] =
                                convertDefaultValue(nodeDefault.getDefaultValue(), nodeDefault.getDefaultValueType());
                    }
                }
            }

            // 填充null值为对应类型的默认值
            fillNullParametersWithDefaults(parameters, methodInfo);

            return parameters;
        } catch (Exception e) {
            throw new RuntimeException("准备方法参数失败", e);
        }
    }

    /**
     * 获取源数据值
     * @param dataMap 数据映射
     * @return 源值
     */
    public Object getSourceValue(DataMap dataMap) {
        Map<String, Object> context = ThreadLocalManager.getExecutionContext();
        if ("input".equals(dataMap.getSourceNodeId())) {
            // 从初始输入获取
            Object input = context.get("input");
            if (input instanceof Map) {
                return ((Map<?, ?>) input).get(dataMap.getSource());
            }
            return null;
        } else {
            // 从前置节点结果获取
            Object nodeResult = context.get(dataMap.getSourceNodeId());
            if (nodeResult instanceof Map) {
                return ((Map<?, ?>) nodeResult).get(dataMap.getSource());
            }
            return nodeResult;
        }
    }

    /**
     * 查找参数在方法签名中的索引位置
     * @param methodInfo 方法信息
     * @param parameterName 参数名
     * @return 索引位置
     */
    public int findParameterIndex(MethodInfo methodInfo, String parameterName) {
        try {
            List<ParameterInfo> parameters = methodInfo.getParameters();
            for (int i = 0; i < parameters.size(); i++) {
                if (parameters.get(i).getName().equals(parameterName)) {
                    return i;
                }
            }
            return -1; // 未找到参数
        } catch (Exception e) {
            throw new RuntimeException("查找参数索引失败", e);
        }
    }

    /**
     * 类型转换
     * @param value 原始值
     * @param targetType 目标类型
     * @return 转换后的值
     */
    public Object convertValueType(Object value, String targetType) {
        if (value == null) return null;
        if (targetType == null) return value;

        try {
            return switch (targetType.toLowerCase()) {
                case "string" -> value.toString();
                case "integer", "int" -> Integer.valueOf(value.toString());
                case "double" -> Double.valueOf(value.toString());
                case "boolean" -> Boolean.valueOf(value.toString());
                case "long" -> Long.valueOf(value.toString());
                default -> value; // 保持原类型
            };
        } catch (NumberFormatException e) {
            log.warn("类型转换失败: {} -> {}", value.getClass().getSimpleName(), targetType);
            return value;
        }
    }

    /**
     * 转换默认值
     * @param defaultValue 默认值字符串
     * @param valueType 值类型
     * @return 转换后的默认值
     */
    public Object convertDefaultValue(String defaultValue, NodeDefaults.DefaultValueType valueType) {
        if (defaultValue == null) return null;

        try {
            return switch (valueType) {
                case String -> defaultValue;
                case Integer -> Integer.valueOf(defaultValue);
                case Double -> Double.valueOf(defaultValue);
                case Boolean -> Boolean.valueOf(defaultValue);
            };
        } catch (NumberFormatException e) {
            log.warn("默认值转换失败: {} -> {}", defaultValue, valueType);
            return defaultValue;
        }
    }

    /**
     * 为null参数填充默认值
     * @param parameters 参数数组
     * @param methodInfo 方法信息
     */
    public void fillNullParametersWithDefaults(Object[] parameters, MethodInfo methodInfo) {
        try {
            List<ParameterInfo> parametersList = methodInfo.getParameters();

            // 为每个null参数设置对应类型的默认值
            for (int i = 0; i < parameters.length && i < parametersList.size(); i++) {
                if (parameters[i] == null) {
                    ParameterInfo paramInfo = parametersList.get(i);
                    String parameterType = paramInfo.getType();
                    parameters[i] = getDefaultValueForType(parameterType);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("填充默认值失败", e);
        }
    }

    /**
     * 获取指定类型的默认值
     * @param type 类型名称
     * @return 默认值
     */
    public Object getDefaultValueForType(String type) {
        return switch (type.toLowerCase()) {
            case "string" -> "";
            case "integer", "int" -> 0;
            case "double" -> 0.0;
            case "boolean" -> false;
            case "long" -> 0L;
            default -> null;
        };
    }

    /**
     * 查找方法
     * @param clazz 类
     * @param methodName 方法名
     * @param parameterCount 参数数量
     * @return 方法对象
     */
    public Method findMethod(Class<?> clazz, String methodName, int parameterCount) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName) &&
                    method.getParameterCount() == parameterCount) {
                return method;
            }
        }
        return null;
    }

    /**
     * 执行结果类
     */
    public record ExecutionResult(Object result, boolean continueExecution) {

    }

    /**
     * 工作流图内部类
     */
    @Setter
    @Getter
    public static class WorkflowGraph {
        private Map<String, Node> nodeMap;
    }
}

