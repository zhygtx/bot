package com.generalbot.workflow.engine;

import com.generalbot.bot.action.BotActionService;
import com.generalbot.common.context.ThreadLocalManager;
import com.generalbot.plugin.entity.ParameterInfo;
import com.generalbot.workflow.engine.convert.ValueConverterRegistry;
import com.generalbot.workflow.entity.definition.WorkflowDefinition;
import com.generalbot.workflow.entity.definition.WorkflowEdge;
import com.generalbot.workflow.entity.definition.WorkflowNode;
import com.generalbot.workflow.entity.WorkflowInfo;
import com.generalbot.workflow.mapper.WorkflowExecutionMapper;
import com.generalbot.workflow.service.WorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 新工作流执行引擎：按 DAG 边激活执行，普通节点激活全部 success 出边，
 * 分支节点按 Boolean 结果只激活 success/failure 出边；未激活出边按跳过处理，
 * 并级联跳过没有任何活动入边的下游节点；异常直接终止本次执行、记录日志并禁用工作流。
 */
@Slf4j
@Component
public class WorkflowEngine {

    /** Spring 容器，用于向插件实例注入接口类型依赖。 */
    private final ApplicationContext applicationContext;

    /** BOT 动作服务，系统动作 callable 的默认调用目标。 */
    private final BotActionService botActionService;

    /** 统一 callable 注册表，负责描述解析与反射方法解析。 */
    private final CallableRegistry callableRegistry;

    /** 入参值转换器注册表，目前只处理目标类型为 String 的例外转换。 */
    private final ValueConverterRegistry valueConverterRegistry;

    /** 执行记录 Mapper，用于持久化快照式 trace。 */
    private final WorkflowExecutionMapper executionMapper;

    /** 工作流服务，执行失败时把工作流标记为不可用。 */
    private final WorkflowService workflowService;

    /** 工作流专用线程池，避免执行阻塞请求或触发线程。 */
    private final ExecutorService workflowExecutor;

    /** 并发执行信号量，限制同时运行的工作流数量。 */
    private final Semaphore workflowSemaphore;

    /** 单次工作流执行的超时秒数，默认 60。 */
    @Value("${workflow.executor.timeout-seconds:60}")
    private long timeoutSeconds;

    /**
     * 注入引擎依赖。
     * WorkflowService 使用 @Lazy，打断 WorkflowEngine 与 WorkflowServiceImpl 之间的循环依赖。
     */
    public WorkflowEngine(ApplicationContext applicationContext,
                          BotActionService botActionService,
                          CallableRegistry callableRegistry,
                          ValueConverterRegistry valueConverterRegistry,
                          WorkflowExecutionMapper executionMapper,
                          @Lazy WorkflowService workflowService,
                          @Qualifier("workflowExecutor") ExecutorService workflowExecutor,
                          @Qualifier("workflowSemaphore") Semaphore workflowSemaphore) {
        this.applicationContext = applicationContext;
        this.botActionService = botActionService;
        this.callableRegistry = callableRegistry;
        this.valueConverterRegistry = valueConverterRegistry;
        this.executionMapper = executionMapper;
        this.workflowService = workflowService;
        this.workflowExecutor = workflowExecutor;
        this.workflowSemaphore = workflowSemaphore;
    }

    /**
     * 执行工作流并返回执行记录 ID。
     * 先尝试获取信号量限流，再把实际执行提交到工作流线程池并按配置超时等待；
     * 超时会禁用工作流，中断会恢复线程中断标志，执行异常优先原样向上抛。
     *
     * @param workflowInfo 工作流信息
     * @param triggerKey 触发键
     * @param payload 触发载荷
     * @return 执行记录 ID
     * @throws Exception 执行失败
     */
    public Long execute(WorkflowInfo workflowInfo, String triggerKey, Object payload) throws Exception {
        // 拿不到信号量说明并发已打满，直接拒绝而不是让任务无限排队。
        boolean acquired = workflowSemaphore.tryAcquire(500, TimeUnit.MILLISECONDS);
        if (!acquired) {
            throw new RuntimeException("系统繁忙，请稍后重试");
        }
        try {
            // 实际执行放到专用线程池，主线程只负责按超时等待结果。
            CompletableFuture<Long> future = CompletableFuture.supplyAsync(
                    () -> runWorkflow(workflowInfo, triggerKey, payload), workflowExecutor);
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            disableWorkflow(workflowInfo, "工作流执行超时（超过" + timeoutSeconds + "秒）");
            throw new RuntimeException("工作流执行超时（超过" + timeoutSeconds + "秒）");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("工作流执行被中断", e);
        } catch (ExecutionException | CompletionException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            if (cause instanceof Exception exception) {
                throw exception;
            }
            throw new RuntimeException(cause);
        } finally {
            workflowSemaphore.release();
        }
    }

    /**
     * 在工作流线程内执行完整流程：设置用户上下文、创建快照记录器、执行 DAG，
     * 成功或失败分别收尾，最后持久化执行记录并清理 ThreadLocal。
     */
    private Long runWorkflow(WorkflowInfo workflowInfo, String triggerKey, Object payload) {
        ThreadLocalManager.setUserId(workflowInfo.getUserId());
        Map<String, Object> context = new HashMap<>();
        context.put("input", payload);
        Map<String, Object> pluginInstances = new HashMap<>();
        JsonExecutionRecorder recorder = new JsonExecutionRecorder(executionMapper, workflowInfo, triggerKey);
        try {
            long startTime = System.currentTimeMillis();
            recorder.workflowStarted(workflowInfo, triggerKey, startTime, payload);
            try {
                executeGraph(workflowInfo.getDefinition(), context, pluginInstances, recorder);
                recorder.workflowCompleted(System.currentTimeMillis());
            } catch (Throwable t) {
                recorder.workflowFailed(System.currentTimeMillis(), t);
                disableWorkflow(workflowInfo, errorMessage(t));
            }
            return recorder.save();
        } finally {
            ThreadLocalManager.clear();
        }
    }

    /**
     * 按 DAG 拓扑顺序执行全部可达节点。
     * 执行状态统一放在 ExecutionState 中；队列里的节点逐个出队执行，
     * 已经跳过或已经完成的节点直接忽略，避免重复执行。
     */
    private void executeGraph(WorkflowDefinition definition,
                              Map<String, Object> context,
                              Map<String, Object> pluginInstances,
                              ExecutionRecorder recorder) {
        ExecutionState state = buildExecutionState(definition);
        while (!state.queue.isEmpty()) {
            executeNode(state, state.queue.poll(), context, pluginInstances, recorder);
        }
    }

    /**
     * 构建单次执行的图状态，并校验连线的两端节点必须存在。
     * pending 初始化为目标节点的入边总数，activeIncoming 初始化为 0，
     * 后续由 activateEdge/skipEdge 共同递减与累加。
     */
    private ExecutionState buildExecutionState(WorkflowDefinition definition) {
        if (definition == null || definition.getNodes() == null || definition.getNodes().isEmpty()) {
            throw new RuntimeException("工作流定义不能为空");
        }
        ExecutionState state = new ExecutionState();
        for (WorkflowNode node : definition.getNodes()) {
            state.nodeMap.put(node.getId(), node);
            state.pending.put(node.getId(), 0);
            state.activeIncoming.put(node.getId(), 0);
            state.outgoing.put(node.getId(), new ArrayList<>());
        }
        for (WorkflowEdge edge : definition.getEdges()) {
            if (!state.nodeMap.containsKey(edge.getFrom()) || !state.nodeMap.containsKey(edge.getTo())) {
                throw new RuntimeException("连线引用了不存在的节点：" + edge.getFrom() + " -> " + edge.getTo());
            }
            state.pending.merge(edge.getTo(), 1, Integer::sum);
            state.outgoing.get(edge.getFrom()).add(edge);
        }
        seedStartQueue(state, definition);
        return state;
    }

    /**
     * 播种初始执行队列：有触发节点时从触发节点开始；
     * 没有触发节点时把入度为零的节点全部作为入口（用于测试无触发的工作流）。
     */
    private void seedStartQueue(ExecutionState state, WorkflowDefinition definition) {
        WorkflowNode triggerNode = definition.getNodes().stream()
                .filter(node -> callableRegistry.describe(node.getCallable()).getKind()
                        == CallableDescriptor.CallableKind.TRIGGER)
                .findFirst()
                .orElse(null);
        if (triggerNode != null) {
            enqueue(state, triggerNode.getId());
            return;
        }
        definition.getNodes().forEach(node -> {
            if (state.pending.get(node.getId()) == 0) {
                enqueue(state, node.getId());
            }
        });
    }

    /**
     * 去重入队：queuedNodes 防止同一节点在真正出队前被重复加入队列。
     */
    private void enqueue(ExecutionState state, String nodeId) {
        if (state.queuedNodes.add(nodeId)) {
            state.queue.add(nodeId);
        }
    }

    /**
     * 执行单个节点并推进后续连线。
     * 节点成功先把结果写入共享 context，再根据分支结果激活或跳过出边；
     * 节点执行或记录失败都会向上抛异常，由外层终止整次执行并禁用工作流。
     */
    private void executeNode(ExecutionState state, String nodeId,
                             Map<String, Object> context,
                             Map<String, Object> pluginInstances,
                             ExecutionRecorder recorder) {
        if (state.skippedNodes.contains(nodeId) || state.completedNodes.contains(nodeId)) {
            return;
        }
        WorkflowNode node = state.nodeMap.get(nodeId);
        CallableDescriptor descriptor = callableRegistry.describe(node.getCallable());
        long startTime = System.currentTimeMillis();
        recorder.nodeStarted(node, descriptor, startTime);

        PreparedInput prepared = null;
        Object result;
        try {
            prepared = prepareNodeInput(node, descriptor, context);
            result = invokeNode(node, descriptor, prepared.args(), context, pluginInstances);
            context.put(node.getId(), result);
            recorder.nodeCompleted(node, System.currentTimeMillis(), prepared.inputLog(), result);
        } catch (Throwable t) {
            recorder.nodeFailed(node, System.currentTimeMillis(),
                    prepared == null ? null : prepared.inputLog(), t);
            throw t;
        }
        state.completedNodes.add(nodeId);

        List<WorkflowEdge> activatedEdges = selectActivatedEdges(state, node, descriptor, result);
        activateAndSkipOutgoing(state, nodeId, activatedEdges);
    }

    /**
     * 选择本次激活的出边：普通节点激活全部非 failure 出边；
     * 分支节点要求返回 Boolean，按 true/false 只激活 success/failure 端口。
     */
    private List<WorkflowEdge> selectActivatedEdges(ExecutionState state,
                                                    WorkflowNode node,
                                                    CallableDescriptor descriptor,
                                                    Object result) {
        List<WorkflowEdge> activated = new ArrayList<>();
        if (Boolean.TRUE.equals(node.getBranch())) {
            if (!(result instanceof Boolean bool)) {
                throw new RuntimeException("分支节点 " + descriptor.getName() + " 的返回值不是 Boolean");
            }
            String port = bool ? "success" : "failure";
            for (WorkflowEdge edge : state.outgoing.get(node.getId())) {
                if (port.equals(edgePort(edge))) {
                    activated.add(edge);
                }
            }
        } else {
            for (WorkflowEdge edge : state.outgoing.get(node.getId())) {
                if (!"failure".equals(edgePort(edge))) {
                    activated.add(edge);
                }
            }
        }
        return activated;
    }

    /**
     * 推进来源节点的出边：选中的出边走激活逻辑，未选中的走跳过逻辑。
     * 两条路径共用 pending 计数，保证目标节点的每条入边都被处理一次。
     */
    private void activateAndSkipOutgoing(ExecutionState state, String nodeId, List<WorkflowEdge> activatedEdges) {
        Set<String> activatedKeys = new HashSet<>();
        for (WorkflowEdge edge : activatedEdges) {
            activatedKeys.add(edgeKey(edge));
            activateEdge(state, edge);
        }
        for (WorkflowEdge edge : state.outgoing.get(nodeId)) {
            if (!activatedKeys.contains(edgeKey(edge))) {
                skipEdge(state, edge);
            }
        }
    }

    /**
     * 激活一条出边：累加目标节点的已激活入边数并递减剩余入边数；
     * 入边全部处理完且节点未被跳过、未完成时，把它加入执行队列。
     */
    private void activateEdge(ExecutionState state, WorkflowEdge edge) {
        if (!state.processedEdges.add(edgeKey(edge))) {
            return;
        }
        state.activeIncoming.merge(edge.getTo(), 1, Integer::sum);
        int remain = decrementPending(state, edge);
        if (remain == 0 && !state.skippedNodes.contains(edge.getTo())
                && !state.completedNodes.contains(edge.getTo())) {
            enqueue(state, edge.getTo());
        }
    }

    /**
     * 跳过一条出边：递减目标节点的剩余入边数。
     * 若目标节点仍有活动入边，入边全部处理完后它仍会执行；
     * 若没有任何活动入边，则标记为跳过，并递归跳过它的下游出边。
     */
    private void skipEdge(ExecutionState state, WorkflowEdge edge) {
        if (!state.processedEdges.add(edgeKey(edge))) {
            return;
        }
        int remain = decrementPending(state, edge);
        if (remain != 0 || state.completedNodes.contains(edge.getTo())) {
            return;
        }
        if (state.activeIncoming.getOrDefault(edge.getTo(), 0) > 0) {
            if (!state.skippedNodes.contains(edge.getTo())) {
                enqueue(state, edge.getTo());
            }
            return;
        }
        if (state.skippedNodes.add(edge.getTo())) {
            for (WorkflowEdge nextEdge : state.outgoing.getOrDefault(edge.getTo(), List.of())) {
                skipEdge(state, nextEdge);
            }
        }
    }

    /**
     * 递减目标节点的剩余入边数并返回剩余值；下限为 0，防止重复处理出现负数。
     */
    private int decrementPending(ExecutionState state, WorkflowEdge edge) {
        int remain = Math.max(0, state.pending.getOrDefault(edge.getTo(), 0) - 1);
        state.pending.put(edge.getTo(), remain);
        return remain;
    }

    /**
     * 归一化连线端口：null/空白按 success 处理，兼容未显式设置端口的历史定义。
     */
    private String edgePort(WorkflowEdge edge) {
        return edge.getPort() == null || edge.getPort().isBlank() ? "success" : edge.getPort();
    }

    /**
     * 生成连线唯一键：来源 + 目标 + 端口，防止同一连线被激活/跳过逻辑重复处理。
     */
    private String edgeKey(WorkflowEdge edge) {
        return edge.getFrom() + "\u0000" + edge.getTo() + "\u0000" + edgePort(edge);
    }

    /**
     * 为任务节点准备参数：触发节点没有方法参数，只把 config 作为输入日志；
     * 普通节点按参数列表逐项解析来源/默认值，并记录实际入参供 trace 展示。
     */
    private PreparedInput prepareNodeInput(WorkflowNode node,
                                           CallableDescriptor descriptor,
                                           Map<String, Object> context) {
        if (descriptor.getKind() == CallableDescriptor.CallableKind.TRIGGER) {
            return new PreparedInput(node.getConfig() == null ? Map.of() : node.getConfig(), new Object[0]);
        }
        List<ParameterInfo> parameters = descriptor.getParameters();
        Object[] args = new Object[parameters.size()];
        Map<String, Object> inputLog = new HashMap<>();
        for (int i = 0; i < parameters.size(); i++) {
            ParameterInfo param = parameters.get(i);
            Object value = resolveParamValue(node, param, i, context);
            args[i] = value;
            inputLog.put(param.getName(), value);
        }
        return new PreparedInput(inputLog, args);
    }

    /**
     * 解析单个参数值：优先取 source 来源，其次取 defaultValue；
     * 都为空且参数允许 nullable 时返回 null，否则报错；最后交给转换器处理例外转换。
     */
    private Object resolveParamValue(WorkflowNode node, ParameterInfo param, int index, Map<String, Object> context) {
        var input = node.getInputs().stream()
                .filter(item -> Integer.valueOf(index).equals(item.getParamIndex()))
                .findFirst()
                .orElse(null);
        Object value;
        if (input != null && input.getSource() != null && !input.getSource().isBlank()) {
            value = resolveSource(input.getSource(), context);
        } else if (input != null && input.getDefaultValue() != null) {
            value = input.getDefaultValue();
        } else if (param.isNullable()) {
            return null;
        } else {
            throw new RuntimeException("参数 " + param.getName() + " 未配置数据来源或默认值");
        }
        return valueConverterRegistry.convert(value, param.getType());
    }

    /**
     * 按点号路径从共享 context 取值：第一段是 input 或节点 ID，
     * 后续段支持 Map 键和对象字段；任意一段取不到返回 null，由参数规则决定是否报错。
     */
    private Object resolveSource(String source, Map<String, Object> context) {
        String[] parts = source.split("\\.");
        Object current = "input".equals(parts[0]) ? context.get("input") : context.get(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (current == null) {
                return null;
            }
            if (current instanceof Map<?, ?> map) {
                current = map.get(parts[i]);
            } else {
                current = readField(current, parts[i]);
            }
        }
        return current;
    }

    /**
     * 反射读取对象字段；字段不存在时尝试无参 getter，失败时抛出带节点上下文的异常。
     */
    private Object readField(Object target, String fieldName) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (NoSuchFieldException e) {
            try {
                String getter = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                Method method = target.getClass().getMethod(getter);
                return method.invoke(target);
            } catch (Exception ex) {
                throw new RuntimeException("无法读取字段 " + fieldName + "：" + ex.getMessage(), ex);
            }
        } catch (Exception e) {
            throw new RuntimeException("无法读取字段 " + fieldName + "：" + e.getMessage(), e);
        }
    }

    /**
     * 分发节点调用：触发节点直接返回当前载荷；插件方法从插件实例缓存获取实例并注入依赖；
     * 其余系统节点调用 BotActionService 上的方法。
     */
    private Object invokeNode(WorkflowNode node,
                              CallableDescriptor descriptor,
                              Object[] args,
                              Map<String, Object> context,
                              Map<String, Object> pluginInstances) {
        if (descriptor.getKind() == CallableDescriptor.CallableKind.TRIGGER) {
            if (node.getCallable().startsWith("system:botEvent:")) {
                return context.get("input");
            }
            if (CallableRegistry.SCHEDULE_KEY.equals(node.getCallable())) {
                return context.get("input");
            }
            throw new RuntimeException("未知触发节点：" + node.getCallable());
        }

        CallableRegistry.ResolvedCallable resolved = callableRegistry.resolve(node.getCallable());
        if (resolved.pluginVersion() != null) {
            ThreadLocalManager.setPluginId(resolved.pluginVersion().getPluginId());
            Object instance = getOrCreatePluginInstance(resolved, pluginInstances);
            return invokeWithError(instance, resolved.method(), args, descriptor);
        }
        return invokeWithError(botActionService, resolved.method(), args, descriptor);
    }

    /**
     * 反射调用并统一包装错误，优先透传真实 cause，避免 InvocationTargetException 掩盖业务异常。
     */
    private Object invokeWithError(Object target, Method method, Object[] args, CallableDescriptor descriptor) {
        try {
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new RuntimeException("节点 " + descriptor.getName() + " 执行失败：" + cause.getMessage(), cause);
        }
    }

    /**
     * 按插件 ID + 版本 + 类名缓存插件实例，避免同一工作流重复创建对象。
     */
    private Object getOrCreatePluginInstance(CallableRegistry.ResolvedCallable resolved,
                                             Map<String, Object> pluginInstances) {
        String cacheKey = resolved.pluginVersion().getPluginId() + ":" + resolved.pluginVersion().getVersion()
                + ":" + resolved.className();
        return pluginInstances.computeIfAbsent(cacheKey, key -> {
            try {
                Class<?> clazz = Class.forName(resolved.className(), true,
                        callableRegistry.getClassLoader(resolved.pluginVersion()));
                Object instance = clazz.getDeclaredConstructor().newInstance();
                injectServices(instance);
                return instance;
            } catch (Exception e) {
                throw new RuntimeException("创建插件实例失败：" + resolved.className(), e);
            }
        });
    }

    /**
     * 把 Spring 中匹配的 Bean 注入插件实例的接口类型字段；
     * 注入失败保留字段原值，插件可能自带默认实现。
     */
    private void injectServices(Object instance) {
        Class<?> currentClass = instance.getClass();
        while (currentClass != null && currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.getType().isInterface()) {
                    try {
                        Object bean = applicationContext.getBean(field.getType());
                        field.set(instance, bean);
                    } catch (Exception e) {
                        log.debug("插件字段 {} 未注入，保持原值", field.getName());
                    }
                }
            }
            currentClass = currentClass.getSuperclass();
        }
    }

    /**
     * 禁用工作流并写入失败原因；禁用本身失败只记录错误日志，避免掩盖原始执行异常。
     */
    private void disableWorkflow(WorkflowInfo workflowInfo, String reason) {
        try {
            workflowService.disable(workflowInfo.getId(), reason);
        } catch (Exception e) {
            log.error("禁用工作流失败：{}", workflowInfo.getId(), e);
        }
    }

    /**
     * 提取可读错误信息：优先取 cause 的 message，无消息时退化为异常类名。
     */
    private String errorMessage(Throwable error) {
        Throwable cause = error.getCause() != null ? error.getCause() : error;
        String message = cause.getMessage();
        return message == null || message.isBlank() ? cause.getClass().getName() : message;
    }

    /**
     * 节点输入与参数数组：inputLog 用于 trace 展示，args 用于反射调用。
     */
    private record PreparedInput(Map<String, Object> inputLog, Object[] args) {
    }

    /**
     * 单次执行的可变图状态，集中保存 DAG 遍历所需的计数与集合。
     * pending 是目标节点尚未处理的入边数；activeIncoming 是已经激活的入边数，
     * 用来区分“还有活动路径”与“整条分支都被跳过”两种情况。
     */
    private static final class ExecutionState {
        /** 节点 ID -> 节点 */
        final Map<String, WorkflowNode> nodeMap = new HashMap<>();

        /** 节点 ID -> 尚未处理的入边数 */
        final Map<String, Integer> pending = new HashMap<>();

        /** 节点 ID -> 已激活的入边数 */
        final Map<String, Integer> activeIncoming = new HashMap<>();

        /** 节点 ID -> 出边列表 */
        final Map<String, List<WorkflowEdge>> outgoing = new HashMap<>();

        /** 已处理连线集合，激活与跳过共用，防止重复处理 */
        final Set<String> processedEdges = new HashSet<>();

        /** 已被判定为不可达的节点 */
        final Set<String> skippedNodes = new HashSet<>();

        /** 已执行完成的节点 */
        final Set<String> completedNodes = new HashSet<>();

        /** 已进入队列的节点 */
        final Set<String> queuedNodes = new HashSet<>();

        /** 待执行节点的 FIFO 队列 */
        final Queue<String> queue = new ArrayDeque<>();
    }
}
