package com.example.demo.util;

import com.example.demo.api.BotActionService;
import com.example.demo.pojo.plugin.MethodClassInfo;
import com.example.demo.pojo.plugin.MethodInfo;
import com.example.demo.pojo.plugin.ParameterInfo;
import com.example.demo.pojo.plugin.PluginVersion;
import com.example.demo.pojo.workflow.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.*;

/**
 * 工作流工具类
 */
@Slf4j
@Component
public class WorkflowUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    // 节点执行超时时间（毫秒）
    private static final long NODE_TIMEOUT_MS = 5000;

    // 缓存已加载的类加载器，避免重复加载
    private final Map<String, URLClassLoader> classLoaderCache = new ConcurrentHashMap<>();

    // 缓存已查找的方法，避免重复反射遍历
    private final Map<String, Method> methodCache = new ConcurrentHashMap<>();

    // Spring 应用上下文
    private final ApplicationContext applicationContext;
    // BOT动作扫描器
    private final BotActionScanner botActionScanner;

    public WorkflowUtil(ApplicationContext applicationContext, BotActionScanner botActionScanner) {
        this.applicationContext = applicationContext;
        this.botActionScanner = botActionScanner;
    }

    /**
     * 关闭并移除指定插件的所有类加载器（所有版本）
     * @param pluginId 插件 ID
     */
    public void closeAllClassLoaderForPlugin(String pluginId) {
        List<String> keysToRemove = classLoaderCache.keySet().stream()
                .filter(key -> key.startsWith(pluginId + ":"))
                .toList();
        
        for (String key : keysToRemove) {
            URLClassLoader classLoader = classLoaderCache.remove(key);
            if (classLoader != null) {
                try {
                    classLoader.close();
                    log.info("已关闭插件类加载器：{}", key);
                } catch (Exception e) {
                    log.error("关闭插件类加载器失败：{}", key, e);
                }
            }
        }
    }

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
     * @return 执行结果
     */
    public JsonNode executeNodesInTopologicalOrder(WorkflowGraph graph)  throws Exception{
        return executeNodesInTopologicalOrder(graph, null);
    }

    /**
     * 按拓扑顺序执行节点
     * @param graph 工作流图
     * @param botEventData BOT事件数据
     * @return 执行结果
     */
    public JsonNode executeNodesInTopologicalOrder(WorkflowGraph graph, Object botEventData)  throws Exception{
        if (graph == null) {
            log.warn("工作流图为 null，直接返回空结果");
            return mapper.createObjectNode();
        }
            
        // 使用线程池执行整个工作流，确保所有节点在同一个线程中运行
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            CompletableFuture<JsonNode> future = CompletableFuture.supplyAsync(() -> {
                try {
                    // 确保执行上下文已初始化
                    Map<String, Object> context = ThreadLocalManager.getExecutionContext();
                    // 存储BOT事件数据
                    if (botEventData != null) {
                        context.put("botEvent", botEventData);
                        log.info("已存储BOT事件数据到上下文");
                    }
                        
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
                        // 获取入度为 0 的节点
                        String currentNodeId = getNextZeroInDegreeNode(inDegreeBuckets);
                        if (currentNodeId == null) {
                            break; // 没有更多可执行的节点
                        }
                
                        Node currentNode = nodeMap.get(currentNodeId);
                        log.info("执行节点：{} (第{}个)", currentNodeId, ++processedCount);
                
                        // 直接在当前线程中执行节点，不使用线程池
                        ExecutionResult executionResult = executeSingleNode(currentNode);
                
                        if (!executionResult.continueExecution()) {
                            // 结束整个工作流
                            log.info("工作流执行被条件终止");
                            return mapper.createObjectNode();
                        }
                
                        if (executionResult.result() != null) {
                            // 如果是最后一个节点，保存结果
                            if (currentNode.getNextNodeId() == null || currentNode.getNextNodeId().isEmpty()) {
                                finalResults.add(mapper.valueToTree(executionResult.result()));
                            }
                
                            // 更新后续节点的入度
                            updateSuccessorNodesInDegree(currentNode, nodeToInDegree, inDegreeBuckets);
                        } else {
                            // 条件判断为 BREAK，结束当前分支
                            log.info("跳过节点{}的后续分支", currentNodeId);
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
                        return finalResults.get(0);
                    } else {
                        // 多个结束节点，返回结果数组
                        return mapper.valueToTree(finalResults);
                    }
                } catch (Exception e) {
                    throw new CompletionException(e);
                } finally {
                    // 清理ThreadLocal变量
                    ThreadLocalManager.clear();
                }
            }, executor);

            try {
                // 设置整个工作流的超时时间，为单个节点超时时间的n倍（n为节点数量）
                int nodeCount = graph.getNodeMap().size();
                long workflowTimeoutMs = NODE_TIMEOUT_MS * nodeCount;
                return future.get(workflowTimeoutMs, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                int nodeCount = graph.getNodeMap().size();
                long workflowTimeoutMs = NODE_TIMEOUT_MS * nodeCount;
                log.error("工作流执行超时：超过{}ms", workflowTimeoutMs);
                future.cancel(true);
                throw new RuntimeException("工作流执行超时（超过" + workflowTimeoutMs + "毫秒）");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("工作流执行被中断");
                throw new RuntimeException("工作流执行被中断", e);
            } catch (CompletionException | ExecutionException e) {
                log.error("工作流执行异常", e.getCause());
                Throwable cause = e.getCause();
                if (cause instanceof Exception) {
                    throw (Exception) cause;
                } else {
                    throw new RuntimeException("工作流执行异常", cause);
                }
            }
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
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
        Set<String> zeroDegreeNodes = inDegreeBuckets.get(0);
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
     * @return 执行结果和是否继续执行
     * @throws Exception 执行过程中的异常
     */
    public ExecutionResult executeSingleNode(Node node) throws Exception {
        // 处理BOT事件节点
        if (Node.NodeType.botEvent.equals(node.getNodeType())) {
            // 处理定时节点
            if ("scheduledEvent".equals(node.getBotEventName())) {
                log.info("执行定时节点: {}", node.getId());
                // 定时任务无上下文，直接跳过·
                // 将空结果存入上下文，以便后续节点调用
                ThreadLocalManager.getExecutionContext().put(node.getId(), new Object());
                // 检查条件
                Condition.Action action = checkConditions(node);
                if (action == Condition.Action.END) {
                    log.info("条件判断结果: 结束整个工作流");
                    return new ExecutionResult(null, false);
                } else if (action == Condition.Action.BREAK) {
                    log.info("条件判断结果: 结束当前分支");
                    return new ExecutionResult(null, true);
                }
                return new ExecutionResult(new Object(), true);
            }
            
            // 处理普通BOT事件节点
            log.info("执行BOT事件节点: {}", node.getId());
            // 从上下文获取预存储的botEvent数据
            Map<String, Object> context = ThreadLocalManager.getExecutionContext();
            Object botEventData = context.get("botEvent");
            if (botEventData == null) {
                log.warn("BOT事件节点执行时，上下文中无botEvent数据");
                botEventData = new Object();
            }
            // 将数据存入上下文，以便后续节点调用
            context.put(node.getId(), botEventData);
            // 检查条件
            Condition.Action action = checkConditions(node);
            if (action == Condition.Action.END) {
                log.info("条件判断结果: 结束整个工作流");
                return new ExecutionResult(null, false);
            } else if (action == Condition.Action.BREAK) {
                log.info("条件判断结果: 结束当前分支");
                return new ExecutionResult(null, true);
            }
            return new ExecutionResult(botEventData, true);
        }
        
        // 处理BOT动作节点
        if (Node.NodeType.botAction.equals(node.getNodeType())) {
            log.info("执行BOT动作节点: {}", node.getId());
            // 获取BotActionService实例
            BotActionService botActionService = applicationContext.getBean(BotActionService.class);
            
            // 获取方法信息
            MethodInfo methodInfo = node.getMethodInfo();
            String methodName;
            
            // 如果没有方法信息，尝试根据botActionName获取方法
            if (methodInfo == null) {
                if (node.getBotActionName() != null) {
                    methodName = node.getBotActionName();
                    log.info("根据botActionName获取方法: {}", methodName);
                } else {
                    throw new IllegalStateException("BOT动作节点" + node.getId() + "缺少方法信息和botActionName");
                }
            } else {
                methodName = methodInfo.getName();
            }
            
            // 准备方法参数
            Object[] parameters;
            if (methodInfo != null) {
                parameters = prepareMethodParameters(node, methodInfo);
            } else {
                // 根据方法名确定参数数量
                int paramCount = getBotActionMethodParamCount(methodName);
                parameters = new Object[paramCount];
                
                // 处理数据映射
                if (node.getDataMaps() != null) {
                    log.debug("处理BOT动作节点数据映射：{}", node.getDataMaps().size());
                    for (DataMap dataMap : node.getDataMaps()) {
                        Object sourceValue = getSourceValue(dataMap);
                        log.debug("获取源值：{}", sourceValue);
                        
                        // 优先使用 paramIndex
                        Integer paramIndex = dataMap.getParamIndex();
                        int targetIndex;
                        
                        if (paramIndex != null && paramIndex >= 0 && paramIndex < parameters.length) {
                            targetIndex = paramIndex;
                        } else {
                            // 对于BOT动作，尝试根据参数名确定索引
                            targetIndex = getBotActionParamIndex(methodName, dataMap.getTargetParamName());
                        }
                        
                        log.debug("目标参数索引：{}", targetIndex);
                        
                        if (targetIndex >= 0 && targetIndex < parameters.length) {
                            // 获取参数值
                            Object paramValue = convertValueType(sourceValue, dataMap.getTargetType());
                            parameters[targetIndex] = paramValue;
                            log.debug("设置参数 [{}] = {}", targetIndex, paramValue);
                        } else {
                            log.warn("目标索引无效：targetIndex={}, parameters.length={}", targetIndex, parameters.length);
                        }
                    }
                }
                
                // 处理默认值
                if (node.getNodeDefaults() != null) {
                    log.debug("处理BOT动作节点默认值：{}", node.getNodeDefaults().size());
                    for (NodeDefaults nodeDefault : node.getNodeDefaults()) {
                        Integer paramIndex = nodeDefault.getParamIndex();
                        if (paramIndex != null && paramIndex < parameters.length &&
                                parameters[paramIndex] == null) {
                            Object defaultValue = convertDefaultValue(nodeDefault.getDefaultValue(), nodeDefault.getDefaultValueType());
                            parameters[paramIndex] = defaultValue;
                            log.debug("使用默认值设置参数 [{}] = {}", paramIndex, defaultValue);
                        }
                    }
                }
            }
            
            // 反射调用方法
            Method method = findMethod(BotActionService.class, methodName, parameters.length);
            if (method == null) {
                throw new NoSuchMethodException("找不到BOT动作方法: " + methodName);
            }
            
            method.setAccessible(true);
            Object result = method.invoke(botActionService, parameters);
            log.debug("BOT动作方法调用结果: {}", result);
            
            // 将结果存入上下文
            ThreadLocalManager.getExecutionContext().put(node.getId(), result);
            
            // 检查条件
            Condition.Action action = checkConditions(node);
            if (action == Condition.Action.END) {
                log.info("条件判断结果: 结束整个工作流");
                return new ExecutionResult(null, false);
            } else if (action == Condition.Action.BREAK) {
                log.info("条件判断结果: 结束当前分支");
                return new ExecutionResult(null, true);
            }
            
            return new ExecutionResult(result, true);
        }
        
        // 处理普通插件节点
        PluginVersion pluginVersion = node.getPluginVersion();
        if (pluginVersion == null) {
            throw new IllegalStateException("节点" + node.getId() + "缺少插件版本信息");
        }

        // 加载类加载器
        URLClassLoader classLoader = getClassLoader(pluginVersion);

        // 获取方法信息
        MethodInfo methodInfo = node.getMethodInfo();
        MethodClassInfo methodClassInfo = node.getMethodClassInfo();

        if (methodInfo == null || methodClassInfo == null) {
            throw new IllegalStateException("节点" + node.getId() + "缺少方法信息");
        }

        // 准备方法参数
        Object[] parameters = prepareMethodParameters(node, methodInfo);

        // 反射调用方法
        Class<?> clazz = classLoader.loadClass(methodClassInfo.getClassName());
        Method method = findMethod(clazz, methodInfo.getName(), parameters.length);

        if (method == null) {
            throw new NoSuchMethodException("找不到方法: " + methodInfo.getName() +
                    " 在类: " + methodClassInfo.getClassName());
        }

        method.setAccessible(true);
        // 使用线程本地缓存获取或创建实例
        Object instance = getOrCreateInstance(pluginVersion, methodClassInfo, clazz);

        log.debug("调用方法: {}.{} 参数数量: {}",
                methodClassInfo.getClassName(), method.getName(), parameters.length);

        Object result = method.invoke(instance, parameters);
        log.debug("方法调用结果: {}", result);
        // 先将结果存入上下文，以便条件判断使用
        ThreadLocalManager.getExecutionContext().put(node.getId(), result);
        
        // 检查条件（基于插件返回值）
        Condition.Action action = checkConditions(node);
        if (action == Condition.Action.END) {
            log.info("条件判断结果: 结束整个工作流");
            return new ExecutionResult(null, false);
        } else if (action == Condition.Action.BREAK) {
            log.info("条件判断结果: 结束当前分支");
            return new ExecutionResult(null, true);
        }
        
        return new ExecutionResult(result, true);
    }

    /**
     * 检查节点的条件
     * @param node 节点信息
     * @return 执行动作
     */
    public Condition.Action checkConditions(Node node) {
        // 从节点中获取条件
        Condition condition = node.getCondition();
        if (condition == null) {
            return Condition.Action.CONTINUE;
        }

        // 获取节点执行结果
        Map<String, Object> context = ThreadLocalManager.getExecutionContext();
        Object nodeResult = context.get(node.getId());
        
        // 检查返回值是否为布尔值
        if (nodeResult instanceof Boolean) {
            boolean result = (Boolean) nodeResult;
            return result ? condition.getTrueAction() : condition.getFalseAction();
        }
        
        return Condition.Action.CONTINUE;
    }

    /**
     * 从线程本地缓存获取或创建实例
     * @param pluginVersion 插件版本信息
     * @param methodClassInfo 方法类信息
     * @param clazz 类对象
     * @return 实例对象
     * @throws Exception 实例创建异常
     */
    public Object getOrCreateInstance(PluginVersion pluginVersion, MethodClassInfo methodClassInfo, Class<?> clazz) throws Exception {
        // 包含插件版本的缓存键
        String version = pluginVersion.getVersion() != null ? pluginVersion.getVersion() : "unknown";
        String cacheKey = pluginVersion.getPluginId() + ":" + version + ":" + methodClassInfo.getClassName();
        Map<String, Object> instanceMap = ThreadLocalManager.getMethodInstanceCache();

        Object instance = instanceMap.get(cacheKey);
        if (instance == null) {
            instance = clazz.getDeclaredConstructor().newInstance();

            // 自动注入服务（识别接口字段）
            injectServices(instance);

            instanceMap.put(cacheKey, instance);
        }

        return instance;
    }

    private void injectServices(Object instance) {
        try {
            Class<?> clazz = instance.getClass();

            // 遍历所有字段
            for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);

                // 检查字段是否是接口类型
                Class<?> fieldType = field.getType();
                if (fieldType.isInterface()) {
                    // 尝试从 Spring 容器获取实现类
                    Object bean;
                    try {
                        bean = applicationContext.getBean(fieldType);
                    } catch (Exception e) {
                        // 没有这个 Bean，跳过
                        continue;
                    }

                    // 替换字段的值
                    field.set(instance, bean);
                    log.debug("已为插件 {} 自动注入接口：{} -> {}",
                            instance.getClass().getSimpleName(),
                            fieldType.getSimpleName(),
                            bean.getClass().getSimpleName());
                }
            }
        } catch (Exception e) {
            log.debug("插件服务注入失败（可能是正常的）: {}", instance.getClass().getSimpleName());
        }
    }

    /**
     * 获取或创建类加载器
     * @param pluginVersion 插件版本信息
     * @return 类加载器
     */
    public URLClassLoader getClassLoader(PluginVersion pluginVersion) {
        String pluginId = pluginVersion.getPluginId();
        String version = pluginVersion.getVersion() != null ? pluginVersion.getVersion() : "unknown";
        String cacheKey = pluginId + ":" + version;

        return classLoaderCache.computeIfAbsent(cacheKey, key -> {
            try {
                // 修复：使用URI构造URL以避免弃用警告
                URL jarUrl = new File(pluginVersion.getPath()).toURI().toURL();
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

            // 记录参数总数
            log.debug("方法签名参数总数：{}", parameterCount);
            for (int i = 0; i < parameterCount; i++) {
                log.debug("参数[{}] 名称：{}, 类型：{}",
                        i, parametersList.get(i).getName(), parametersList.get(i).getType());
            }

            // 处理数据映射
            if (node.getDataMaps() != null) {
                log.debug("数据映射配置数量：{}", node.getDataMaps().size());
                for (DataMap dataMap : node.getDataMaps()) {
                    log.debug("处理数据映射：sourceNodeId={}, sourcePath={}, targetParamName={}, paramIndex={}, targetType={}",
                            dataMap.getSourceNodeId(),
                            dataMap.getSourcePath(),
                            dataMap.getTargetParamName(),
                            dataMap.getParamIndex(),
                            dataMap.getTargetType());

                    Object sourceValue = getSourceValue(dataMap);
                    log.debug("获取源值：{}", sourceValue);

                    // 优先使用 paramIndex
                    Integer paramIndex = dataMap.getParamIndex();
                    int targetIndex;

                    if (paramIndex != null && paramIndex >= 0 && paramIndex < parameters.length) {
                        targetIndex = paramIndex;
                    } else {
                        // 回退到通过参数名查找
                        targetIndex = findParameterIndex(methodInfo, dataMap.getTargetParamName());
                    }

                    log.debug("目标参数索引：{}", targetIndex);

                    if (targetIndex >= 0 && targetIndex < parameters.length) {
                        // 获取参数值
                        Object paramValue = convertValueType(sourceValue, dataMap.getTargetType());
                        parameters[targetIndex] = paramValue;
                        log.debug("设置参数 [{}] = {}", targetIndex, paramValue);
                    } else {
                        log.warn("目标索引无效：targetIndex={}, parameters.length={}", targetIndex, parameters.length);
                    }
                }
            }

            // 处理默认值
            if (node.getNodeDefaults() != null) {
                log.debug("默认值配置数量：{}", node.getNodeDefaults().size());
                for (NodeDefaults nodeDefault : node.getNodeDefaults()) {
                    Integer paramIndex = nodeDefault.getParamIndex();
                    if (paramIndex != null && paramIndex < parameters.length &&
                            parameters[paramIndex] == null) {
                        Object defaultValue = convertDefaultValue(nodeDefault.getDefaultValue(), nodeDefault.getDefaultValueType());
                        parameters[paramIndex] = defaultValue;
                        log.debug("使用默认值设置参数 [{}] = {}", paramIndex, defaultValue);
                    }
                }
            }

            // 填充 null 值为对应类型的默认值
            fillNullParametersWithDefaults(parameters, methodInfo);

            // 详细打印最终参数数组
            log.debug("最终参数数组 (长度:{})", parameters.length);
            for (int i = 0; i < parameters.length; i++) {
                log.debug("参数 [{}] = {} (类型：{})", i, parameters[i],
                        parameters[i] != null ? parameters[i].getClass().getSimpleName() : "null");
            }

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
        Object sourceValue;
        
        if ("input".equals(dataMap.getSourceNodeId())) {
            // 从初始输入获取
            sourceValue = context.get("input");
        } else {
            // 从前置节点结果获取
            sourceValue = context.get(dataMap.getSourceNodeId());
        }
        
        // 处理 sourcePath，支持 value 前缀和嵌套属性
        String sourcePath = dataMap.getSourcePath();
        if (sourcePath != null && !sourcePath.isEmpty()) {
            sourceValue = getPropertyValue(sourceValue, sourcePath);
        }
        
        return sourceValue;
    }
    
    /**
     * 根据属性路径获取对象的属性值
     * 支持 value 前缀表示返回值本身，如 value 或 value.id.name
     */
    public Object getPropertyValue(Object obj, String path) {
        if (obj == null || path == null || path.isEmpty()) {
            return null;
        }
        
        // 处理 value 前缀
        if (path.startsWith("value")) {
            // 如果路径就是 "value"，直接返回对象本身
            if ("value".equals(path)) {
                return obj;
            }
            // 否则，去掉 "value." 前缀，获取后续的属性路径
            String subPath = path.substring("value.".length());
            return getNestedPropertyValue(obj, subPath);
        }
        
        // 没有 value 前缀的情况，直接处理路径
        return getNestedPropertyValue(obj, path);
    }
    
    /**
     * 获取嵌套属性值
     */
    private Object getNestedPropertyValue(Object obj, String path) {
        if (obj == null || path == null || path.isEmpty()) {
            return obj;
        }
        
        // 处理 Map 类型
        if (obj instanceof Map<?, ?>) {
            String[] parts = path.split("\\.");
            Object current = obj;
            
            for (String part : parts) {
                if (current instanceof Map) {
                    current = ((Map<?, ?>) current).get(part);
                } else {
                    // 如果中间节点不是 Map，尝试使用反射
                    current = getFieldValue(current, part);
                }
                if (current == null) {
                    break;
                }
            }
            
            return current;
        }
        
        // 处理普通对象，使用反射
        String[] parts = path.split("\\.");
        Object current = obj;
        
        for (String part : parts) {
            current = getFieldValue(current, part);
            if (current == null) {
                break;
            }
        }
        
        return current;
    }

    /**
     * 使用反射获取字段值
     */
    private Object getFieldValue(Object obj, String fieldName) {
        if (obj == null || fieldName == null) {
            return null;
        }
        
        // 尝试获取字段
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            // 尝试获取 getter 方法
            try {
                String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                java.lang.reflect.Method method = obj.getClass().getMethod(methodName);
                return method.invoke(obj);
            } catch (Exception ex) {
                return null;
            }
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
            if (parameterName == null) {
                return -1;
            }
            
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
                case Integer -> Integer.valueOf(defaultValue);
                case Double -> Double.valueOf(defaultValue);
                case Boolean -> Boolean.valueOf(defaultValue);
                case Long -> Long.valueOf(defaultValue);
                default -> defaultValue;
            };
        } catch (NumberFormatException e) {
            log.warn("默认值转换失败：{} -> {}", defaultValue, valueType);
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
        if (type == null) return null;

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
     * 获取BOT动作方法的参数数量
     * @param methodName 方法名
     * @return 参数数量
     */
    private int getBotActionMethodParamCount(String methodName) {
        return botActionScanner.getMethodParamCount(methodName);
    }

    /**
     * 获取BOT动作方法的参数索引
     * @param methodName 方法名
     * @param paramName 参数名
     * @return 参数索引
     */
    private int getBotActionParamIndex(String methodName, String paramName) {
        return botActionScanner.getMethodParamIndex(methodName, paramName);
    }

    /**
     * 查找方法（带缓存优化）
     * @param clazz 类
     * @param methodName 方法名
     * @param parameterCount 参数数量
     * @return 方法对象
     */
    public Method findMethod(Class<?> clazz, String methodName, int parameterCount) {
        // 构建缓存键：类名 + 方法名 + 参数数量
        String cacheKey = clazz.getName() + ":" + methodName + ":" + parameterCount;
        
        // 先从缓存中获取，避免重复反射操作
        return methodCache.computeIfAbsent(cacheKey, key -> {
            log.debug("方法缓存未命中，通过反射查找：{}.{} (参数数:{})",
                    clazz.getSimpleName(), methodName, parameterCount);
            
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equals(methodName) &&
                        method.getParameterCount() == parameterCount) {
                    return method;
                }
            }
            return null;
        });
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

