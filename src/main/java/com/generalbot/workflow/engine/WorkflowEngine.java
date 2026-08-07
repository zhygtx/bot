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
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 新工作流执行引擎：按 DAG 边激活执行，普通节点激活全部 success 出边，
 * 分支节点按 Boolean 结果只激活 success/failure 出边，异常直接终止并禁用工作流。
 */
@Slf4j
@Component
public class WorkflowEngine {

    private final ApplicationContext applicationContext;
    private final BotActionService botActionService;
    private final CallableRegistry callableRegistry;
    private final ValueConverterRegistry valueConverterRegistry;
    private final WorkflowExecutionMapper executionMapper;
    private final WorkflowService workflowService;
    private final ExecutorService workflowExecutor;
    private final Semaphore workflowSemaphore;

    @Value("${workflow.executor.timeout-seconds:60}")
    private long timeoutSeconds;

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
     * 执行工作流并返回执行记录ID。
     * @param workflowInfo 工作流信息
     * @param triggerKey 触发键
     * @param payload 触发载荷
     * @return 执行记录ID
     * @throws Exception 执行失败
     */
    public Long execute(WorkflowInfo workflowInfo, String triggerKey, Object payload) throws Exception {
        boolean acquired = workflowSemaphore.tryAcquire(500, TimeUnit.MILLISECONDS);
        if (!acquired) {
            throw new RuntimeException("系统繁忙，请稍后重试");
        }
        try {
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

    private void executeGraph(WorkflowDefinition definition,
                              Map<String, Object> context,
                              Map<String, Object> pluginInstances,
                              ExecutionRecorder recorder) {
        if (definition == null || definition.getNodes() == null || definition.getNodes().isEmpty()) {
            throw new RuntimeException("工作流定义不能为空");
        }
        Map<String, WorkflowNode> nodeMap = new HashMap<>();
        Map<String, Integer> pending = new HashMap<>();
        Map<String, List<WorkflowEdge>> outgoing = new HashMap<>();

        for (WorkflowNode node : definition.getNodes()) {
            nodeMap.put(node.getId(), node);
            pending.put(node.getId(), 0);
            outgoing.put(node.getId(), new ArrayList<>());
        }
        for (WorkflowEdge edge : definition.getEdges()) {
            if (!nodeMap.containsKey(edge.getFrom()) || !nodeMap.containsKey(edge.getTo())) {
                throw new RuntimeException("连线引用了不存在的节点：" + edge.getFrom() + " -> " + edge.getTo());
            }
            pending.merge(edge.getTo(), 1, Integer::sum);
            outgoing.get(edge.getFrom()).add(edge);
        }

        WorkflowNode triggerNode = definition.getNodes().stream()
                .filter(node -> callableRegistry.describe(node.getCallable()).getKind()
                        == CallableDescriptor.CallableKind.TRIGGER)
                .findFirst()
                .orElse(null);

        Queue<String> queue = new ArrayDeque<>();
        if (triggerNode != null) {
            queue.add(triggerNode.getId());
        } else {
            definition.getNodes().forEach(node -> {
                if (pending.get(node.getId()) == 0) {
                    queue.add(node.getId());
                }
            });
        }

        while (!queue.isEmpty()) {
            String nodeId = queue.poll();
            WorkflowNode node = nodeMap.get(nodeId);
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

            List<WorkflowEdge> activated = new ArrayList<>();
            if (Boolean.TRUE.equals(node.getBranch())) {
                if (!(result instanceof Boolean bool)) {
                    throw new RuntimeException("分支节点 " + descriptor.getName() + " 的返回值不是 Boolean");
                }
                String port = bool ? "success" : "failure";
                for (WorkflowEdge edge : outgoing.get(nodeId)) {
                    if (port.equals(edge.getPort())) {
                        activated.add(edge);
                    }
                }
            } else {
                for (WorkflowEdge edge : outgoing.get(nodeId)) {
                    if (!"failure".equals(edge.getPort())) {
                        activated.add(edge);
                    }
                }
            }

            for (WorkflowEdge edge : activated) {
                int remain = pending.get(edge.getTo()) - 1;
                pending.put(edge.getTo(), remain);
                if (remain == 0) {
                    queue.add(edge.getTo());
                }
            }
        }
    }

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

    private Object invokeWithError(Object target, Method method, Object[] args, CallableDescriptor descriptor) {
        try {
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new RuntimeException("节点 " + descriptor.getName() + " 执行失败：" + cause.getMessage(), cause);
        }
    }

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

    private void disableWorkflow(WorkflowInfo workflowInfo, String reason) {
        try {
            workflowService.disable(workflowInfo.getId(), reason);
        } catch (Exception e) {
            log.error("禁用工作流失败：{}", workflowInfo.getId(), e);
        }
    }

    private String errorMessage(Throwable error) {
        Throwable cause = error.getCause() != null ? error.getCause() : error;
        String message = cause.getMessage();
        return message == null || message.isBlank() ? cause.getClass().getName() : message;
    }

    /**
     * 节点输入与参数数组。
     */
    private record PreparedInput(Map<String, Object> inputLog, Object[] args) {
    }
}
