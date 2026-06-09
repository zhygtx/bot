package com.example.demo.util;

import com.example.demo.api.BotActionService;
import com.example.demo.pojo.entity.log.BigText;
import com.example.demo.pojo.entity.log.NodeLog;
import com.example.demo.pojo.entity.log.WorkflowLog;
import com.example.demo.pojo.entity.plugin.MethodClassInfo;
import com.example.demo.pojo.entity.plugin.MethodInfo;
import com.example.demo.pojo.entity.plugin.ParameterInfo;
import com.example.demo.pojo.entity.plugin.PluginVersion;
import com.example.demo.pojo.entity.workflow.*;
import com.example.demo.scanner.BotActionScanner;
import com.example.demo.service.WorkflowLogService;
import com.example.demo.service.WorkflowService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Stream;

/**
 * 工作流工具类
 * 负责工作流的验证、构建、执行和管理
 */
@Slf4j
@Component
public class WorkflowUtil {

    /** JSON 对象映射器，用于序列化/反序列化 */
    private static final ObjectMapper mapper = new ObjectMapper();
    
    /** 单个节点执行超时时间（毫秒） */
    private static final long NODE_TIMEOUT_MS = 5000;

    /** 缓存已加载的插件类加载器，key格式：pluginId:version */
    private final Map<String, URLClassLoader> classLoaderCache = new ConcurrentHashMap<>();
    
    /** 缓存已查找的方法，避免重复反射操作 */
    private final Map<String, Method> methodCache = new ConcurrentHashMap<>();

    /** Spring 应用上下文，用于获取 Bean 和依赖注入 */
    private final ApplicationContext applicationContext;
    
    /** BOT 动作扫描器，用于获取 BOT 动作方法的元数据 */
    private final BotActionScanner botActionScanner;
    
    /** 工作流服务，用于更新工作流状态 */
    private final WorkflowService workflowService;
    
    /** 全局工作流执行线程池 */
    private final ExecutorService workflowExecutor;
    
    /** 并发限流信号量，控制同时执行的工作流数量 */
    private final Semaphore workflowSemaphore;

    /** 工作流日志服务，用于保存工作流执行日志 */
    private final WorkflowLogService workflowLogService;

    /**
     * 构造函数
     * @param applicationContext Spring 应用上下文
     * @param botActionScanner BOT 动作扫描器
     * @param workflowService 工作流服务
     * @param workflowExecutor 工作流执行线程池
     * @param workflowSemaphore 并发限流信号量
     */
    public WorkflowUtil(ApplicationContext applicationContext, BotActionScanner botActionScanner,
                        @Lazy WorkflowService workflowService,
                        @Qualifier("workflowExecutor") ExecutorService workflowExecutor,
                        @Qualifier("workflowSemaphore") Semaphore workflowSemaphore, WorkflowLogService workflowLogService) {
        this.applicationContext = applicationContext;
        this.botActionScanner = botActionScanner;
        this.workflowService = workflowService;
        this.workflowExecutor = workflowExecutor;
        this.workflowSemaphore = workflowSemaphore;
        this.workflowLogService = workflowLogService;
    }

    /**
     * 关闭并移除指定插件的所有类加载器（所有版本）
     * 用于插件卸载或更新时释放资源
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
                    log.debug("已关闭插件类加载器：{}", key);
                } catch (Exception e) {
                    log.error("关闭插件类加载器失败：{}", key, e);
                }
            }
        }
    }

    /**
     * 验证工作流配置的有效性
     * 检查工作流信息和节点列表是否为空
     * @param workflowInfo 工作流信息
     * @return true-验证通过，false-验证失败
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
        log.debug("工作流配置验证通过");
        return true;
    }

    /**
     * 构建工作流依赖关系图
     * 计算每个节点的入度，用于后续的拓扑排序
     *
     * @param workflowInfo 工作流信息
     * @return 工作流图对象
     */
    public WorkflowGraph buildWorkflowGraph(WorkflowInfo workflowInfo) {
        WorkflowGraph graph = new WorkflowGraph();
        Map<String, Node> nodeMap = new HashMap<>();
        for (Node node : workflowInfo.getNodes()) {
            nodeMap.put(node.getId(), node);
            node.setInDegree(calculateInDegree(node, workflowInfo.getNodes()));
        }
        graph.setNodeMap(nodeMap);
        return graph;
    }

    /**
     * 计算节点的入度
     * 统计有多少个节点的前置节点包含目标节点
     *
     * @param targetNode 目标节点
     * @param allNodes 所有节点列表
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
     * 执行工作流
     * 获取并发许可后执行工作流，执行完成后释放许可并清理线程本地变量
     *
     * @param workflowInfo 工作流信息
     * @param key 输入数据的键名
     * @param object 输入数据对象
     * @return 工作流执行结果的 JSON 节点
     * @throws Exception 执行过程中的异常
     */
    public JsonNode executeWorkflow(WorkflowInfo workflowInfo, String key, Object object) throws Exception {
        log.debug("开始执行工作流：{} (ID: {})", workflowInfo.getName(), workflowInfo.getId());
        
        boolean acquired = workflowSemaphore.tryAcquire(500, TimeUnit.MILLISECONDS);
        if (!acquired) {
            log.warn("工作流 {} 执行被拒绝：系统并发数已达上限", workflowInfo.getId());
            throw new RuntimeException("系统繁忙，请稍后重试");
        }
        
        try {
            ThreadLocalManager.setUserId(workflowInfo.getUserId());
            WorkflowGraph graph = buildWorkflowGraph(workflowInfo);
            
            // 使用全局线程池执行工作流，日志创建和保存都在异步线程中完成
            JsonNode result = executeNodesInTopologicalOrder(workflowInfo, graph, key, object);
            log.debug("工作流执行完成：{}", result);

            return result;
        } finally {
            workflowSemaphore.release();
            ThreadLocalManager.clear();
        }
    }

    /**
     * 按拓扑顺序执行节点
     * 使用全局线程池异步执行节点列表，日志创建和保存都在同一线程中完成
     * @param workflowInfo 工作流信息
     * @param graph 工作流图
     * @param key 输入数据的键名
     * @param object 输入数据对象
     * @return 工作流执行结果的 JSON 节点
     * @throws Exception 执行过程中的异常
     */
    public JsonNode executeNodesInTopologicalOrder(WorkflowInfo workflowInfo, WorkflowGraph graph, String key, Object object) throws Exception {
        if (graph == null) {
            log.warn("工作流图为 null，直接返回空结果");
            return mapper.createObjectNode();
        }

        CompletableFuture<JsonNode> future = CompletableFuture.supplyAsync(() -> {
            long workflowStartTime = System.currentTimeMillis();
            try {
                setupExecutionContext(workflowInfo, key, object);
                Map<String, Node> nodeMap = graph.getNodeMap();
                
                // 创建工作流日志（在异步线程中创建，避免ThreadLocal问题）
                WorkflowLog workflowLog = WorkflowLog.builder()
                        .workflowId(workflowInfo.getId())
                        .userId(workflowInfo.getUserId())
                        .expectedNodeCount(workflowInfo.getNodes().size())
                        .startTime(workflowStartTime)
                        .initialContext(object != null ? mapper.writeValueAsString(object) : null)
                        .workflowName(workflowInfo.getName())
                        .isError(false)
                        .build();
                ThreadLocalManager.setWorkflowLog(workflowLog);
                
                List<String> executionOrder = topologicalSort(nodeMap);

                // 使用线程池执行节点列表
                JsonNode result = executeNodeList(workflowInfo, nodeMap, executionOrder);
                
                // 更新执行时间和实际节点数
                workflowLog.setExecutionTime(System.currentTimeMillis() - workflowStartTime);
                workflowLog.setActualNodeCount(ThreadLocalManager.getNodeLogList().size());
                
                // 保存日志（在同一异步线程中保存）
                workflowLogService.add(workflowLog, ThreadLocalManager.getNodeLogList(), getBigTextList());
                log.debug("工作流日志保存完成");
                
                return result;
            } catch (Throwable e) {
                log.error("工作流执行异常，尝试保存已执行的节点日志", e);
                // 即使发生异常，也要保存已执行的节点日志
                try {
                    WorkflowLog workflowLog = ThreadLocalManager.getWorkflowLog();
                    if (workflowLog != null) {
                        workflowLog.setExecutionTime(System.currentTimeMillis() - workflowStartTime);
                        workflowLog.setActualNodeCount(ThreadLocalManager.getNodeLogList().size());
                        workflowLog.setIsError(true);
                        
                        // 将完整堆栈保存为 bigText，key 写入 errorLog
                        String errorKey = saveErrorAsBigText(workflowInfo.getId(), e);
                        workflowLog.setErrorLog(errorKey);
                        
                        workflowLogService.add(workflowLog, ThreadLocalManager.getNodeLogList(), getBigTextList());
                        log.debug("异常情况下工作流日志保存完成");
                    }
                } catch (Exception saveEx) {
                    log.error("保存工作流日志失败", saveEx);
                }
                throw new CompletionException(e instanceof Exception ? e : new RuntimeException(e));
            } finally {
                ThreadLocalManager.clear();
            }
        }, workflowExecutor);

        int nodeCount = graph.getNodeMap().size();
        long workflowTimeoutMs = NODE_TIMEOUT_MS * nodeCount;

        try {
            return future.get(workflowTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            log.error("工作流执行超时：超过{}ms", workflowTimeoutMs);
            workflowService.editDisableReason(workflowInfo.getId(), "工作流执行超时（超过" + workflowTimeoutMs + "毫秒）");
            future.cancel(true);
            throw new RuntimeException("工作流执行超时（超过" + workflowTimeoutMs + "毫秒）");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.debug("工作流执行被中断");
            workflowService.editDisableReason(workflowInfo.getId(), "工作流执行被中断:" + e.getMessage());
            throw new RuntimeException("工作流执行被中断", e);
        } catch (CompletionException | ExecutionException e) {
            log.error("工作流执行异常", e.getCause());
            workflowService.editDisableReason(workflowInfo.getId(), "工作流执行异常:" + e.getMessage());
            Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            } else {
                throw new RuntimeException("工作流执行异常", cause);
            }
        }
    }

    /**
     * 设置执行上下文
     * 将用户 ID 和输入数据存储到 ThreadLocal 中
     * @param workflowInfo 工作流信息
     * @param key 输入数据的键名
     * @param object 输入数据对象
     */
    private void setupExecutionContext(WorkflowInfo workflowInfo, String key, Object object) {
        Map<String, Object> context = ThreadLocalManager.getExecutionContext();
        ThreadLocalManager.setUserId(workflowInfo.getUserId());
        if (object != null) {
            context.put(key, object);
            log.debug("已存储 BOT 事件数据到上下文: {}", object);
        }
    }

    /**
     * 拓扑排序
     * 使用 Kahn 算法对节点进行拓扑排序，确保节点按依赖顺序执行
     * @param nodeMap 节点映射表
     * @return 排序后的节点 ID 列表
     */
    public List<String> topologicalSort(Map<String, Node> nodeMap) {
        List<String> executionOrder = new ArrayList<>();
        Map<String, Integer> nodeToInDegree = new HashMap<>();
        Queue<String> zeroDegreeQueue = new LinkedList<>();

        for (Node node : nodeMap.values()) {
            int inDegree = node.getInDegree();
            nodeToInDegree.put(node.getId(), inDegree);
            if (inDegree == 0) {
                zeroDegreeQueue.offer(node.getId());
            }
        }

        while (!zeroDegreeQueue.isEmpty()) {
            String currentNodeId = zeroDegreeQueue.poll();
            executionOrder.add(currentNodeId);

            Node currentNode = nodeMap.get(currentNodeId);
            if (currentNode.getNextNodeId() != null) {
                for (String nextNodeId : currentNode.getNextNodeId()) {
                    int newInDegree = nodeToInDegree.get(nextNodeId) - 1;
                    nodeToInDegree.put(nextNodeId, newInDegree);
                    if (newInDegree == 0) {
                        zeroDegreeQueue.offer(nextNodeId);
                    }
                }
            }
        }

        if (executionOrder.size() != nodeMap.size()) {
            log.warn("拓扑排序异常：预期{}个节点，实际排序{}个节点，可能存在环", 
                    nodeMap.size(), executionOrder.size());
        }

        return executionOrder;
    }

    /**
     * 执行节点列表
     * 按拓扑排序后的顺序依次执行节点，收集最终结果
     * @param workflowInfo 工作流信息
     * @param nodeMap 节点映射表
     * @param executionOrder 节点执行顺序列表
     * @return 工作流最终执行结果的 JSON 节点
     */
    private JsonNode executeNodeList(WorkflowInfo workflowInfo, Map<String, Node> nodeMap, List<String> executionOrder) {
        List<JsonNode> finalResults = new ArrayList<>();
        int processedCount = 0;
        Set<String> skippedNodes = new HashSet<>(); // 记录需要跳过的节点

        for (String currentNodeId : executionOrder) {
            // 如果当前节点被标记为跳过，则跳过执行
            if (skippedNodes.contains(currentNodeId)) {
                log.debug("跳过节点 {}（因条件分支终止）", currentNodeId);
                continue;
            }

            Date startTime = new Date();

            Node currentNode = nodeMap.get(currentNodeId);
            log.debug("执行节点：{} (第{}个)", currentNodeId, ++processedCount);

            String methodName = resolveNodeMethodName(currentNode);
            String methodDescription = resolveNodeMethodDescription(currentNode);
            
            NodeLog nodeLog = NodeLog.builder()
                    .nodeId(currentNodeId)
                    .methodId(currentNode.getMethodId())
                    .order(processedCount)
                    .methodName(methodName)
                    .methodDescription(methodDescription)
                    .isError(false)
                    .build();
            ThreadLocalManager.addNodeLog(nodeLog);
            
            ExecutionResult executionResult;
            try {
                executionResult = executeSingleNode(currentNode);
            } catch (Throwable e) {
                // 节点执行失败，将完整堆栈保存为 bigText，key 作为节点输出
                log.error("节点 {} 执行失败", currentNodeId, e);
                
                String errorKey = saveErrorAsBigText(currentNodeId, e);
                nodeLog.setOutput(errorKey);
                nodeLog.setIsError(true);
                nodeLog.setExecutionTime(System.currentTimeMillis() - startTime.getTime());
                ThreadLocalManager.addNodeLog(nodeLog);
                
                // 同步标记工作流日志为错误
                WorkflowLog wfLog = ThreadLocalManager.getWorkflowLog();
                if (wfLog != null) {
                    wfLog.setIsError(true);
                }
                
                // 更新工作流禁用原因（仅保留简要信息）
                String errorMsg = String.format("节点 %s 执行失败：%s", currentNodeId, e.getMessage());
                workflowService.editDisableReason(workflowInfo.getId(), errorMsg);
                
                // 节点执行失败后，终止工作流执行
                log.debug("节点执行失败，终止工作流执行");
                return mapper.createObjectNode();
            }

            // 记录节点执行时间（成功执行时）
            nodeLog.setExecutionTime(System.currentTimeMillis() - startTime.getTime());
            ThreadLocalManager.addNodeLog(nodeLog);

            // 处理 END：立即结束整个工作流
            if (!executionResult.continueExecution() && executionResult.result() == null) {
                log.debug("工作流执行被条件终止（END）");
                return mapper.createObjectNode();
            }

            // 处理 BREAK：标记该条件节点的所有后继节点（包括间接后继）为跳过
            if (!executionResult.continueExecution()) {
                log.debug("条件判断结果：结束当前分支（BREAK），跳过节点 {} 的所有后继节点", currentNodeId);
                Set<String> allSuccessors = findAllSuccessors(currentNodeId, nodeMap);
                skippedNodes.addAll(allSuccessors);
                log.debug("已标记 {} 个节点为跳过: {}", allSuccessors.size(), allSuccessors);
                continue;
            }

            if (executionResult.result() != null) {
                if (currentNode.getNextNodeId() == null || currentNode.getNextNodeId().isEmpty()) {
                    finalResults.add(mapper.valueToTree(executionResult.result()));
                }
            } else {
                log.debug("跳过节点{}的后续分支", currentNodeId);
            }
        }

        return buildFinalResult(finalResults);
    }

    /**
     * 构建最终结果
     * 根据结果数量返回不同的 JSON 结构
     * @param finalResults 最终结果列表
     * @return JSON 节点
     */
    private JsonNode buildFinalResult(List<JsonNode> finalResults) {
        if (finalResults.isEmpty()) {
            return mapper.createObjectNode();
        } else if (finalResults.size() == 1) {
            return finalResults.get(0);
        } else {
            return mapper.valueToTree(finalResults);
        }
    }

    /**
     * 执行单个节点
     * 根据节点类型分发到不同的执行方法
     * @param node 节点信息
     * @return 执行结果和是否继续执行的标志
     * @throws Exception 执行过程中的异常
     */
    public ExecutionResult executeSingleNode(Node node) throws Throwable {
        ThreadLocalManager.setPluginId(node.getPluginId());

        return switch (node.getNodeType()) {
            case botEvent -> executeBotEventNode(node);
            case botAction -> executeBotActionNode(node);
            case pluginMethod -> executePluginNode(node);
        };
    }

    /**
     * 执行 BOT 事件节点
     * 处理定时事件和普通 BOT 事件
     * @param node 节点信息
     * @return 执行结果
     */
    private ExecutionResult executeBotEventNode(Node node) {
        log.debug("执行 BOT 事件节点：{}", node.getId());
        NodeLog nodeLog = ThreadLocalManager.getNodeLog(node.getId());

        Object resultData;
        if ("scheduledEvent".equals(node.getEventType())) {
            resultData = new Object();
            ThreadLocalManager.getExecutionContext().put(node.getId(), resultData);
            nodeLog.setInput(node.getScheduledTime() + "秒");
            nodeLog.setOutput(null);
            log.debug("定时节点执行完成");
        } else {
            Map<String, Object> context = ThreadLocalManager.getExecutionContext();
            Object botEventData = context.get("botEvent");
            if (botEventData == null) {
                log.warn("BOT 事件节点执行时，上下文中无 botEvent 数据");
                botEventData = new Object();
            }
            resultData = botEventData;

            try {
                nodeLog.setOutput(mapper.writeValueAsString(resultData));
            } catch (JsonProcessingException e) {
                nodeLog.setOutput(resultData.toString());
            }

            context.put(node.getId(), resultData);
        }

        return checkNodeConditions(node, resultData);
    }

    /**
     * 执行 BOT 动作节点
     * 通过反射调用 BotActionService 中的方法
     * @param node 节点信息
     * @return 执行结果
     * @throws Exception 执行过程中的异常
     */
    private ExecutionResult executeBotActionNode(Node node) throws Exception {
        log.debug("执行 BOT 动作节点：{}", node.getId());

        BotActionService botActionService = applicationContext.getBean(BotActionService.class);
        MethodInfo methodInfo = node.getMethodInfo();
        String methodName = resolveMethodName(node, methodInfo);

        Object[] parameters = prepareBotActionParameters(node, methodInfo, methodName);

        Method method = findMethod(BotActionService.class, methodName, parameters.length);
        if (method == null) {
            throw new NoSuchMethodException("找不到 BOT 动作方法：" + methodName);
        }

        method.setAccessible(true);
        Object result = method.invoke(botActionService, parameters);
        log.debug("BOT 动作方法调用结果：{}", result);

        recordMethodParameters(node, parameters, result);

        ThreadLocalManager.getExecutionContext().put(node.getId(), result);

        return checkNodeConditions(node, result);
    }

    /**
     * 执行插件节点
     * 通过类加载器加载插件类，反射调用方法
     * @param node 节点信息
     * @return 执行结果
     * @throws Exception 执行过程中的异常
     */
    private ExecutionResult executePluginNode(Node node) throws Throwable {
        log.debug("执行插件节点：{}", node.getId());

        PluginVersion pluginVersion = node.getPluginVersion();
        if (pluginVersion == null) {
            throw new IllegalStateException("节点" + node.getId() + "缺少插件版本信息");
        }

        URLClassLoader classLoader = getClassLoader(pluginVersion);
        MethodInfo methodInfo = node.getMethodInfo();
        MethodClassInfo methodClassInfo = node.getMethodClassInfo();

        if (methodInfo == null || methodClassInfo == null) {
            throw new IllegalStateException("节点" + node.getId() + "缺少方法信息");
        }

        Object[] parameters = prepareMethodParameters(node, methodInfo);

        Class<?> clazz = classLoader.loadClass(methodClassInfo.getClassName());
        Method method = findMethod(clazz, methodInfo.getName(), parameters.length);

        if (method == null) {
            throw new NoSuchMethodException("找不到方法：" + methodInfo.getName() +
                    " 在类：" + methodClassInfo.getClassName());
        }

        method.setAccessible(true);
        Object instance = getOrCreateInstance(pluginVersion, methodClassInfo, clazz);

        log.debug("调用方法：{}.{} 参数数量：{} 实例哈希：{}",
                methodClassInfo.getClassName(), method.getName(), parameters.length,
                instance != null ? instance.hashCode() : "null");

        Object result = method.invoke(instance, parameters);
        log.debug("方法调用结果：{}", result);

        recordMethodParameters(node, parameters, result);

        ThreadLocalManager.getExecutionContext().put(node.getId(), result);

        return checkNodeConditions(node, result);
    }

    /**
     * 记录方法参数
     * @param node 节点信息
     * @param parameters 方法参数
     * @param result 方法调用结果
     */
    private void recordMethodParameters(Node node, Object[] parameters, Object result) {
        NodeLog nodeLog = ThreadLocalManager.getNodeLog(node.getId());
        List<String> parameterNames;

        if (node.getBotActionName() != null){
            List<DataMap> dataMaps = node.getDataMaps();
            List<NodeDefaults> nodeDefaults = node.getNodeDefaults();
            parameterNames = Stream.concat(
                    dataMaps != null ? dataMaps.stream()
                            .filter(dm -> dm.getParamIndex() != null)
                            .sorted(Comparator.comparing(DataMap::getParamIndex))
                            .map(DataMap::getTargetParamName) : Stream.empty(),
                    nodeDefaults != null ? nodeDefaults.stream()
                            .filter(nd -> nd.getParamIndex() != null)
                            .sorted(Comparator.comparing(NodeDefaults::getParamIndex))
                            .map(NodeDefaults::getParamName) : Stream.empty()
            ).toList();
        }else {
            List<ParameterInfo> parameterInfos = node.getMethodInfo().getParameters();
            parameterInfos.sort(Comparator.comparing(ParameterInfo::getOrder,
                    Comparator.nullsFirst(Comparator.naturalOrder())));
            parameterNames = new ArrayList<>(parameterInfos.stream().map(ParameterInfo::getName).toList());
        }


        Map<String, Object> methodParameters = new HashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            methodParameters.put(parameterNames.get(i), parameters[i]);
        }
        
        String inputStr;
        try {
            inputStr = mapper.writeValueAsString(methodParameters);
        } catch (JsonProcessingException e) {
            inputStr = methodParameters.toString();
        }
        nodeLog.setInput(handleBigText(inputStr, node.getId()));
        
        String outputStr;
        try {
            outputStr = mapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            outputStr = result.toString();
        }
        nodeLog.setOutput(handleBigText(outputStr, node.getId()));
        
        ThreadLocalManager.addNodeLog(nodeLog);
    }

    private String handleBigText(String data, String nodeId) {
        if (data == null || data.length() <= ThreadLocalManager.BIG_TEXT_THRESHOLD) {
            return data;
        }
        
        String key = ThreadLocalManager.BIG_TEXT_PREFIX + nodeId + ":" + 
                     System.currentTimeMillis() + ":" + 
                     UUID.randomUUID().toString().substring(0, 8);
        ThreadLocalManager.addBigText(key, data);
        return key;
    }

    /**
     * 将异常的完整堆栈信息保存为 bigText，返回 bigText 引用键
     * @param nodeId 关联节点/工作流 ID
     * @param e      异常对象
     * @return bigText 引用键
     */
    private String saveErrorAsBigText(String nodeId, Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        return handleBigText(stackTrace, nodeId);
    }

    private List<BigText> getBigTextList() {
        Map<String, String> cache = ThreadLocalManager.getBigTextCache();
        if (cache == null || cache.isEmpty()) {
            return Collections.emptyList();
        }
        return cache.entrySet().stream()
                .map(entry -> new BigText(entry.getKey(), entry.getValue()))
                .toList();
    }

    /**
     * 解析方法名称
     * 优先使用方法信息中的名称，回退到 botActionName
     * @param node 节点信息
     * @param methodInfo 方法信息
     * @return 方法名称
     */
    private String resolveMethodName(Node node, MethodInfo methodInfo) {
        if (methodInfo != null) {
            return methodInfo.getName();
        } else if (node.getBotActionName() != null) {
            log.debug("根据 botActionName 获取方法：{}", node.getBotActionName());
            return node.getBotActionName();
        } else {
            throw new IllegalStateException("BOT 动作节点" + node.getId() + "缺少方法信息和 botActionName");
        }
    }

    /**
     * 解析节点的方法名
     * 根据节点类型获取对应的方法名
     * @param node 节点信息
     * @return 方法名
     */
    private String resolveNodeMethodName(Node node) {
        MethodInfo methodInfo = node.getMethodInfo();
        if (methodInfo != null && methodInfo.getName() != null) {
            return methodInfo.getName();
        }
        
        return switch (node.getNodeType()) {
            case botAction -> node.getBotActionName() != null ? node.getBotActionName() : "未知动作";
            case botEvent -> node.getBotEventName() != null ? node.getBotEventName() : 
                           (node.getEventType() != null ? node.getEventType() : "未知事件");
            case pluginMethod -> "未知方法";
        };
    }

    /**
     * 解析节点的方法描述
     * 根据节点类型获取对应的方法描述
     * @param node 节点信息
     * @return 方法描述
     */
    private String resolveNodeMethodDescription(Node node) {
        MethodInfo methodInfo = node.getMethodInfo();
        if (methodInfo != null && methodInfo.getDescription() != null) {
            return methodInfo.getDescription();
        }
        
        return switch (node.getNodeType()) {
            case botAction -> node.getBotActionName();
            case botEvent -> node.getBotEventName();
            case pluginMethod -> "插件方法节点";
        };
    }

    /**
     * 准备 BOT 动作参数
     * 处理方法信息存在和不存在两种情况
     * @param node 节点信息
     * @param methodInfo 方法信息
     * @param methodName 方法名称
     * @return 参数数组
     */
    private Object[] prepareBotActionParameters(Node node, MethodInfo methodInfo, String methodName) {
        Object[] parameters;

        if (methodInfo != null) {
            parameters = prepareMethodParameters(node, methodInfo);
        } else {
            int paramCount = getBotActionMethodParamCount(methodName);
            parameters = new Object[paramCount];

            if (node.getDataMaps() != null) {
                log.debug("处理 BOT 动作节点数据映射：{}", node.getDataMaps().size());
                for (DataMap dataMap : node.getDataMaps()) {
                    Object sourceValue = getSourceValue(dataMap);
                    Integer paramIndex = dataMap.getParamIndex();
                    int targetIndex = resolveTargetIndex(paramIndex, parameters.length, methodName, dataMap.getTargetParamName());

                    if (isValidTargetIndex(targetIndex, parameters.length)) {
                        Object paramValue = convertValueType(sourceValue, dataMap.getTargetType());
                        parameters[targetIndex] = paramValue;
                        log.debug("设置参数 [{}] = {}", targetIndex, paramValue);
                    }
                }
            }

            applyNodeDefaults(node, parameters);
        }

        return parameters;
    }

    /**
     * 解析目标参数索引
     * 优先使用 paramIndex，回退到通过参数名查找
     * @param paramIndex 参数索引
     * @param paramLength 参数数组长度
     * @param methodName 方法名称
     * @param paramName 参数名称
     * @return 目标参数索引
     */
    private int resolveTargetIndex(Integer paramIndex, int paramLength, String methodName, String paramName) {
        if (paramIndex != null && paramIndex >= 0 && paramIndex < paramLength) {
            return paramIndex;
        } else {
            return getBotActionParamIndex(methodName, paramName);
        }
    }

    /**
     * 验证目标索引是否有效
     * @param targetIndex 目标索引
     * @param paramLength 参数数组长度
     * @return true-有效，false-无效
     */
    private boolean isValidTargetIndex(int targetIndex, int paramLength) {
        return targetIndex >= 0 && targetIndex < paramLength;
    }

    /**
     * 应用节点默认值
     * 为 null 参数设置默认值
     * @param node 节点信息
     * @param parameters 参数数组
     */
    private void applyNodeDefaults(Node node, Object[] parameters) {
        if (node.getNodeDefaults() != null) {
            log.debug("处理节点默认值：{}", node.getNodeDefaults().size());
            for (NodeDefaults nodeDefault : node.getNodeDefaults()) {
                Integer paramIndex = nodeDefault.getParamIndex();
                if (paramIndex != null && paramIndex < parameters.length && parameters[paramIndex] == null) {
                    Object defaultValue = convertDefaultValue(nodeDefault.getDefaultValue(), nodeDefault.getDefaultValueType());
                    parameters[paramIndex] = defaultValue;
                    log.debug("使用默认值设置参数 [{}] = {}", paramIndex, defaultValue);
                }
            }
        }
    }

    /**
     * 查找节点的所有后继节点（包括间接后继）
     * 使用广度优先搜索遍历所有后继节点
     * @param nodeId 起始节点 ID
     * @param nodeMap 节点映射表
     * @return 所有后继节点 ID 集合
     */
    private Set<String> findAllSuccessors(String nodeId, Map<String, Node> nodeMap) {
        Set<String> successors = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        
        // 获取直接后继节点
        Node node = nodeMap.get(nodeId);
        if (node != null && node.getNextNodeId() != null) {
            queue.addAll(node.getNextNodeId());
        }
        
        // BFS 遍历所有后继节点
        while (!queue.isEmpty()) {
            String currentId = queue.poll();
            if (successors.add(currentId)) { // add 返回 false 表示已存在，避免重复处理
                Node currentNode = nodeMap.get(currentId);
                if (currentNode != null && currentNode.getNextNodeId() != null) {
                    queue.addAll(currentNode.getNextNodeId());
                }
            }
        }
        
        return successors;
    }

    /**
     * 检查节点条件并返回执行结果
     * @param node 节点信息
     * @param result 节点执行结果
     * @return 执行结果对象
     */
    private ExecutionResult checkNodeConditions(Node node, Object result) {
        Condition.Action action = checkConditions(node);
        if (action == Condition.Action.END) {
            log.debug("条件判断结果：结束整个工作流");
            return new ExecutionResult(null, false); // continueExecution=false, result=null 表示 END
        } else if (action == Condition.Action.BREAK) {
            log.debug("条件判断结果：结束当前分支");
            return new ExecutionResult(result, false); // continueExecution=false, result!=null 表示 BREAK
        }
        return new ExecutionResult(result, true);
    }

    /**
     * 检查节点的条件
     * 根据节点返回值判断执行动作
     * @param node 节点信息
     * @return 执行动作（CONTINUE/BREAK/END）
     */
    public Condition.Action checkConditions(Node node) {
        Condition condition = node.getCondition();
        if (condition == null) {
            return Condition.Action.CONTINUE;
        }

        Map<String, Object> context = ThreadLocalManager.getExecutionContext();
        Object nodeResult = context.get(node.getId());
        
        if (nodeResult instanceof Boolean) {
            boolean result = (Boolean) nodeResult;
            return result ? condition.getTrueAction() : condition.getFalseAction();
        }
        
        return Condition.Action.CONTINUE;
    }

    /**
     * 从线程本地缓存获取或创建实例
     * 支持自动注入 Spring Bean
     * @param pluginVersion 插件版本信息
     * @param methodClassInfo 方法类信息
     * @param clazz 类对象
     * @return 实例对象
     * @throws Exception 实例创建异常
     */
    public Object getOrCreateInstance(PluginVersion pluginVersion, MethodClassInfo methodClassInfo, Class<?> clazz) throws Exception {
        String version = pluginVersion.getVersion() != null ? pluginVersion.getVersion() : "unknown";
        String cacheKey = pluginVersion.getPluginId() + ":" + version + ":" + methodClassInfo.getClassName();
        Map<String, Object> instanceMap = ThreadLocalManager.getMethodInstanceCache();

        Object instance = instanceMap.get(cacheKey);
        if (instance == null) {
            instance = clazz.getDeclaredConstructor().newInstance();
            injectServices(instance);
            instanceMap.put(cacheKey, instance);
            log.debug("已创建插件实例：{}", cacheKey);
        } else {
            log.debug("使用缓存的插件实例：{}", cacheKey);
        }

        return instance;
    }

    /**
     * 自动注入 Spring 服务
     * 遍历类层次结构，识别接口字段并从 Spring 容器获取实现类
     * @param instance 需要注入的实例对象
     */
    private void injectServices(Object instance) {
        try {
            Class<?> currentClass = instance.getClass();
            while (currentClass != null && currentClass != Object.class) {
                for (java.lang.reflect.Field field : currentClass.getDeclaredFields()) {
                    field.setAccessible(true);
                    Class<?> fieldType = field.getType();
                    if (fieldType.isInterface()) {
                        Object bean;
                        try {
                            bean = applicationContext.getBean(fieldType);
                        } catch (Exception e) {
                            continue;
                        }
                        field.set(instance, bean);
                        log.debug("已为插件 {} 自动注入接口：{} -> {}",
                                instance.getClass().getSimpleName(),
                                fieldType.getSimpleName(),
                                bean.getClass().getSimpleName());
                    }
                }
                currentClass = currentClass.getSuperclass();
            }
        } catch (Exception e) {
            log.debug("插件服务注入失败（可能是正常的）：{}", instance.getClass().getSimpleName());
        }
    }

    /**
     * 获取或创建类加载器
     * 使用缓存避免重复加载同一插件版本
     * @param pluginVersion 插件版本信息
     * @return URL 类加载器
     */
    public URLClassLoader getClassLoader(PluginVersion pluginVersion) {
        String pluginId = pluginVersion.getPluginId();
        String version = pluginVersion.getVersion() != null ? pluginVersion.getVersion() : "unknown";
        String cacheKey = pluginId + ":" + version;

        return classLoaderCache.computeIfAbsent(cacheKey, key -> {
            try {
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
     * 处理数据映射、默认值和类型转换
     * @param node 节点信息
     * @param methodInfo 方法信息
     * @return 参数数组
     */
    public Object[] prepareMethodParameters(Node node, MethodInfo methodInfo) {
        try {
            List<ParameterInfo> parametersList = methodInfo.getParameters();
            int parameterCount = parametersList.size();
            Object[] parameters = new Object[parameterCount];

            log.debug("方法签名参数总数：{}", parameterCount);
            for (int i = 0; i < parameterCount; i++) {
                log.debug("参数[{}] 名称：{}, 类型：{}",
                        i, parametersList.get(i).getName(), parametersList.get(i).getType());
            }

            if (node.getDataMaps() != null) {
                log.debug("数据映射配置数量：{}", node.getDataMaps().size());
                for (DataMap dataMap : node.getDataMaps()) {
                    log.debug("处理数据映射：sourceNodeId={}, sourcePath={}, targetParamName={}, paramIndex={}, targetType={}",
                            dataMap.getSourceNodeId(), dataMap.getSourcePath(),
                            dataMap.getTargetParamName(), dataMap.getParamIndex(), dataMap.getTargetType());

                    Object sourceValue = getSourceValue(dataMap);
                    log.debug("获取源值：{}", sourceValue);

                    Integer paramIndex = dataMap.getParamIndex();
                    int targetIndex = (paramIndex != null && paramIndex >= 0 && paramIndex < parameters.length)
                            ? paramIndex : findParameterIndex(methodInfo, dataMap.getTargetParamName());

                    log.debug("目标参数索引：{}", targetIndex);

                    if (targetIndex >= 0 && targetIndex < parameters.length) {
                        Object paramValue = convertValueType(sourceValue, dataMap.getTargetType());
                        parameters[targetIndex] = paramValue;
                        log.debug("设置参数 [{}] = {}", targetIndex, paramValue);
                    }
                }
            }

            applyNodeDefaults(node, parameters);
            fillNullParametersWithDefaults(parameters, methodInfo);

            log.debug("最终参数数组 (长度:{})", parameters.length);
            for (int i = 0; i < parameters.length; i++) {
                log.debug("参数 [{}] = {} (类型：{})", i, parameters[i],
                        parameters[i] != null ? parameters[i].getClass().getSimpleName() : "null");
            }

            return parameters;
        } catch (Exception e) {
            log.error("准备方法参数失败", e);
            throw new RuntimeException("准备方法参数失败", e);
        }
    }

    /**
     * 获取源数据值
     * 从执行上下文中获取数据，支持嵌套属性访问
     * @param dataMap 数据映射配置
     * @return 源数据值
     */
    public Object getSourceValue(DataMap dataMap) {
        Map<String, Object> context = ThreadLocalManager.getExecutionContext();
        Object sourceValue = "input".equals(dataMap.getSourceNodeId())
                ? context.get("input")
                : context.get(dataMap.getSourceNodeId());

        String sourcePath = dataMap.getSourcePath();
        if (sourcePath != null && !sourcePath.isEmpty()) {
            sourceValue = getPropertyValue(sourceValue, sourcePath);
        }

        return sourceValue;
    }

    /**
     * 根据属性路径获取对象的属性值
     * 支持 value 前缀表示返回值本身，如 value 或 value.id.name
     * @param obj 对象
     * @param path 属性路径
     * @return 属性值
     */
    public Object getPropertyValue(Object obj, String path) {
        if (obj == null || path == null || path.isEmpty()) {
            return null;
        }

        if (path.startsWith("value")) {
            if ("value".equals(path)) {
                return obj;
            }
            String subPath = path.substring("value.".length());
            return getNestedPropertyValue(obj, subPath);
        }

        return getNestedPropertyValue(obj, path);
    }

    /**
     * 获取嵌套属性值
     * 支持 Map 和普通对象的属性访问
     * @param obj 对象
     * @param path 属性路径
     * @return 嵌套属性值
     */
    private Object getNestedPropertyValue(Object obj, String path) {
        if (obj == null || path == null || path.isEmpty()) {
            return obj;
        }

        if (obj instanceof Map<?, ?>) {
            String[] parts = path.split("\\.");
            Object current = obj;
            for (String part : parts) {
                if (current instanceof Map) {
                    current = ((Map<?, ?>) current).get(part);
                } else {
                    current = getFieldValue(current, part);
                }
                if (current == null) break;
            }
            return current;
        }

        String[] parts = path.split("\\.");
        Object current = obj;
        for (String part : parts) {
            current = getFieldValue(current, part);
            if (current == null) break;
        }
        return current;
    }

    /**
     * 使用反射获取字段值
     * 优先尝试直接访问字段，失败则尝试 getter 方法
     * @param obj 对象
     * @param fieldName 字段名称
     * @return 字段值
     */
    private Object getFieldValue(Object obj, String fieldName) {
        if (obj == null || fieldName == null) {
            return null;
        }

        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
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
     * @return 索引位置，未找到返回 -1
     */
    public int findParameterIndex(MethodInfo methodInfo, String parameterName) {
        if (parameterName == null) {
            return -1;
        }

        List<ParameterInfo> parameters = methodInfo.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            if (parameters.get(i).getName().equals(parameterName)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 类型转换
     * 支持 String、Integer、Double、Boolean、Long 等基础类型
     * @param value 原始值
     * @param targetType 目标类型名称
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
                default -> value;
            };
        } catch (NumberFormatException e) {
            log.warn("类型转换失败: {} -> {}", value.getClass().getSimpleName(), targetType);
            return value;
        }
    }

    /**
     * 转换默认值
     * @param defaultValue 默认值字符串
     * @param valueType 值类型枚举
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
     * 为 null 参数填充默认值
     * 根据参数类型设置对应的默认值
     * @param parameters 参数数组
     * @param methodInfo 方法信息
     */
    public void fillNullParametersWithDefaults(Object[] parameters, MethodInfo methodInfo) {
        try {
            List<ParameterInfo> parametersList = methodInfo.getParameters();
            for (int i = 0; i < parameters.length && i < parametersList.size(); i++) {
                if (parameters[i] == null) {
                    ParameterInfo paramInfo = parametersList.get(i);
                    parameters[i] = getDefaultValueForType(paramInfo.getType());
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
     * 获取 BOT 动作方法的参数数量
     * @param methodName 方法名
     * @return 参数数量
     */
    private int getBotActionMethodParamCount(String methodName) {
        return botActionScanner.getMethodParamCount(methodName);
    }

    /**
     * 获取 BOT 动作方法的参数索引
     * @param methodName 方法名
     * @param paramName 参数名
     * @return 参数索引
     */
    private int getBotActionParamIndex(String methodName, String paramName) {
        return botActionScanner.getMethodParamIndex(methodName, paramName);
    }

    /**
     * 查找方法（带缓存优化）
     * 依次在当前类、父类、接口中查找匹配的方法
     * @param clazz 类对象
     * @param methodName 方法名
     * @param parameterCount 参数数量
     * @return 方法对象，未找到返回 null
     */
    public Method findMethod(Class<?> clazz, String methodName, int parameterCount) {
        String cacheKey = clazz.getName() + ":" + methodName + ":" + parameterCount + ":" + clazz.getClassLoader().hashCode();

        return methodCache.computeIfAbsent(cacheKey, key -> {
            log.debug("方法缓存未命中，通过反射查找：{}.{} (参数数:{})",
                    clazz.getSimpleName(), methodName, parameterCount);

            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equals(methodName) && method.getParameterCount() == parameterCount) {
                    return method;
                }
            }

            Class<?> superClass = clazz.getSuperclass();
            while (superClass != null && superClass != Object.class) {
                for (Method method : superClass.getDeclaredMethods()) {
                    if (method.getName().equals(methodName) && method.getParameterCount() == parameterCount) {
                        log.debug("在父类 {} 中找到方法：{}.{}", superClass.getSimpleName(), clazz.getSimpleName(), methodName);
                        return method;
                    }
                }
                superClass = superClass.getSuperclass();
            }

            for (Class<?> face : clazz.getInterfaces()) {
                for (Method method : face.getMethods()) {
                    if (method.getName().equals(methodName) && method.getParameterCount() == parameterCount) {
                        log.debug("在接口 {} 中找到方法：{}.{}", face.getSimpleName(), clazz.getSimpleName(), methodName);
                        return method;
                    }
                }
            }

            log.warn("未找到方法：{}.{} (参数数:{})", clazz.getSimpleName(), methodName, parameterCount);
            return null;
        });
    }

    /**
     * 执行结果记录类
     * @param result 执行结果
     * @param continueExecution 是否继续执行
     */
    public record ExecutionResult(Object result, boolean continueExecution) {}

    /**
     * 工作流图内部类
     * 存储节点映射关系
     */
    @Setter
    @Getter
    public static class WorkflowGraph {
        /** 节点 ID 到节点对象的映射 */
        private Map<String, Node> nodeMap;
    }
}