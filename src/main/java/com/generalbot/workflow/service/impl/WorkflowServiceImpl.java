package com.generalbot.workflow.service.impl;

import com.generalbot.plugin.entity.ParameterInfo;
import com.generalbot.workflow.dto.WorkflowInfoDto;
import com.generalbot.workflow.engine.CallableRegistry;
import com.generalbot.workflow.engine.CallableDescriptor;
import com.generalbot.workflow.engine.TriggerRegistry;
import com.generalbot.workflow.engine.WorkflowEngine;
import com.generalbot.workflow.entity.definition.ParamInput;
import com.generalbot.workflow.entity.definition.WorkflowDefinition;
import com.generalbot.workflow.entity.definition.WorkflowEdge;
import com.generalbot.workflow.entity.definition.WorkflowNode;
import com.generalbot.workflow.entity.WorkflowInfo;
import com.generalbot.workflow.mapper.WorkflowExecutionMapper;
import com.generalbot.workflow.mapper.WorkflowInfoMapper;
import com.generalbot.workflow.service.WorkflowService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

/**
 * 工作流服务实现。定义整体保存，增删改只操作一行记录。
 */
@Slf4j
@Service
public class WorkflowServiceImpl implements WorkflowService {

    private final WorkflowInfoMapper workflowInfoMapper;
    private final WorkflowExecutionMapper executionMapper;
    private final CallableRegistry callableRegistry;
    private final TriggerRegistry triggerRegistry;
    private final WorkflowEngine workflowEngine;

    /**
     * 构造工作流服务。
     * @param workflowInfoMapper 工作流 Mapper
     * @param executionMapper 执行记录 Mapper
     * @param callableRegistry callable 注册表
     * @param triggerRegistry 触发注册表
     * @param workflowEngine 执行引擎
     */
    public WorkflowServiceImpl(WorkflowInfoMapper workflowInfoMapper,
                               WorkflowExecutionMapper executionMapper,
                               CallableRegistry callableRegistry,
                               TriggerRegistry triggerRegistry,
                               WorkflowEngine workflowEngine) {
        this.workflowInfoMapper = workflowInfoMapper;
        this.executionMapper = executionMapper;
        this.callableRegistry = callableRegistry;
        this.triggerRegistry = triggerRegistry;
        this.workflowEngine = workflowEngine;
    }

    /**
     * 新增工作流：校验定义、计算触发键、插入一行并注册触发。
     */
    @Override
    @Transactional
    public WorkflowInfo add(WorkflowInfo workflowInfo) {
        if (workflowInfo.getId() == null || workflowInfo.getId().isBlank()) {
            workflowInfo.setId(UUID.randomUUID().toString());
        }
        LocalDateTime now = LocalDateTime.now();
        workflowInfo.setCreateTime(now);
        workflowInfo.setUpdateTime(now);
        workflowInfo.setAvailable(true);
        workflowInfo.setDisableReason(null);
        workflowInfo.setTriggerKey(validateAndPrepare(workflowInfo));
        workflowInfoMapper.insert(workflowInfo);
        WorkflowInfo saved = workflowInfoMapper.selectById(workflowInfo.getId());
        triggerRegistry.register(saved);
        return saved;
    }

    /**
     * 删除工作流：先取消触发注册，再删除记录（执行记录级联删除）。
     */
    @Override
    @Transactional
    public int remove(String id) {
        triggerRegistry.unregister(id);
        return workflowInfoMapper.deleteById(id);
    }

    /**
     * 编辑工作流：整份定义原地更新，节点 ID 保持稳定。
     * 节点执行逻辑变化（节点/连线/映射/分支/触发配置等）时删除旧执行记录，避免 trace 对不上新定义；
     * 仅位置或画布视图变化不影响历史日志。
     */
    @Override
    @Transactional
    public int edit(WorkflowInfo workflowInfo) {
        WorkflowInfo existing = workflowInfoMapper.selectById(workflowInfo.getId());
        if (existing == null) {
            throw new RuntimeException("工作流不存在：" + workflowInfo.getId());
        }
        boolean logicChanged = isExecutionLogicChanged(existing.getDefinition(), workflowInfo.getDefinition());
        workflowInfo.setUpdateTime(LocalDateTime.now());
        workflowInfo.setTriggerKey(validateAndPrepare(workflowInfo));
        triggerRegistry.unregister(workflowInfo.getId());
        int result = workflowInfoMapper.update(workflowInfo);
        if (result > 0 && logicChanged) {
            executionMapper.deleteByWorkflowId(workflowInfo.getId());
        }
        WorkflowInfo saved = workflowInfoMapper.selectById(workflowInfo.getId());
        triggerRegistry.register(saved);
        return result;
    }

    /**
     * 启用/禁用工作流，并同步触发注册表。
     */
    @Override
    public int editEnabled(String id, boolean enabled) {
        triggerRegistry.unregister(id);
        int result = workflowInfoMapper.updateEnabled(id, enabled);
        if (enabled && result > 0) {
            WorkflowInfo workflowInfo = workflowInfoMapper.selectById(id);
            triggerRegistry.register(workflowInfo);
        }
        return result;
    }

    /**
     * 禁用工作流并写入原因，同时取消触发注册。
     */
    @Override
    public void disable(String id, String disableReason) {
        triggerRegistry.unregister(id);
        workflowInfoMapper.disable(id, disableReason);
    }

    /**
     * 扫描全部工作流，禁用依赖指定插件的定义。
     */
    @Override
    public void disableWorkflowsByPlugin(String pluginId, String disableReason) {
        for (WorkflowInfo workflowInfo : workflowInfoMapper.selectAll()) {
            if (usesPlugin(workflowInfo.getDefinition(), pluginId)) {
                disable(workflowInfo.getId(), disableReason);
            }
        }
    }

    /**
     * 分页查询工作流，并聚合执行次数、平均耗时、平均节点数。
     */
    @Override
    public PageInfo<WorkflowInfoDto> findAll(String userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<WorkflowInfo> all = workflowInfoMapper.selectAll();
        PageInfo<WorkflowInfo> rawPage = new PageInfo<>(all);

        List<WorkflowInfoDto> dtos = new ArrayList<>();
        for (WorkflowInfo workflowInfo : all) {
            WorkflowInfoDto dto = new WorkflowInfoDto();
            dto.setId(workflowInfo.getId());
            dto.setUserId(workflowInfo.getUserId());
            dto.setName(workflowInfo.getName());
            dto.setEnabled(workflowInfo.getEnabled());
            dto.setAvailable(workflowInfo.getAvailable());
            dto.setDisableReason(workflowInfo.getDisableReason());
            dto.setTriggerKey(workflowInfo.getTriggerKey());
            dto.setCreateTime(workflowInfo.getCreateTime());
            dto.setUpdateTime(workflowInfo.getUpdateTime());
            dto.setNodeCount(workflowInfo.getDefinition() == null
                    ? 0 : workflowInfo.getDefinition().getNodes().size());
            dtos.add(dto);
        }

        List<String> ids = all.stream().map(WorkflowInfo::getId).toList();
        if (!ids.isEmpty()) {
            Map<String, Map<String, Object>> statsMap = new HashMap<>();
            for (Map<String, Object> stats : executionMapper.selectStatsByWorkflowIds(ids)) {
                statsMap.put(String.valueOf(stats.get("workflowId")), stats);
            }
            for (WorkflowInfoDto dto : dtos) {
                Map<String, Object> stats = statsMap.get(dto.getId());
                if (stats != null) {
                    dto.setExecuteCount(toInt(stats.get("executeCount")));
                    dto.setAverageExecutionTime(toInt(stats.get("averageExecutionTime")));
                    dto.setAverageNodeCount(toInt(stats.get("averageNodeCount")));
                } else {
                    dto.setExecuteCount(0);
                    dto.setAverageExecutionTime(0);
                    dto.setAverageNodeCount(0);
                }
            }
        }

        PageInfo<WorkflowInfoDto> pageInfo = new PageInfo<>(dtos);
        pageInfo.setTotal(rawPage.getTotal());
        pageInfo.setPageNum(rawPage.getPageNum());
        pageInfo.setPageSize(rawPage.getPageSize());
        return pageInfo;
    }

    /**
     * 根据 ID 查询工作流完整定义。
     */
    @Override
    public WorkflowInfo findById(String id) {
        return workflowInfoMapper.selectById(id);
    }

    /**
     * 测试执行工作流，返回执行记录 ID。
     */
    @Override
    public Long test(String workflowId) {
        WorkflowInfo workflowInfo = workflowInfoMapper.selectById(workflowId);
        if (workflowInfo == null) {
            throw new RuntimeException("工作流不存在：" + workflowId);
        }
        try {
            return workflowEngine.execute(workflowInfo, "test", null);
        } catch (Exception e) {
            throw new RuntimeException("测试工作流失败：" + e.getMessage(), e);
        }
    }

    /**
     * 校验并准备工作流：名称、节点、连线、环、参数输入，最后计算触发键。
     */
    private String validateAndPrepare(WorkflowInfo workflowInfo) {
        if (workflowInfo.getName() == null || workflowInfo.getName().isBlank()) {
            throw new RuntimeException("工作流名称不能为空");
        }
        WorkflowDefinition definition = workflowInfo.getDefinition();
        if (definition == null || definition.getNodes() == null || definition.getNodes().isEmpty()) {
            throw new RuntimeException("工作流至少需要一个节点");
        }

        Set<String> nodeIds = new HashSet<>();
        WorkflowNode triggerNode = null;
        for (WorkflowNode node : definition.getNodes()) {
            if (!nodeIds.add(node.getId())) {
                throw new RuntimeException("节点ID重复：" + node.getId());
            }
            CallableDescriptor descriptor = callableRegistry.describe(node.getCallable());
            if (descriptor.getKind() == CallableDescriptor.CallableKind.TRIGGER) {
                if (triggerNode != null) {
                    throw new RuntimeException("一个工作流只能有一个触发节点");
                }
                triggerNode = node;
                if (Boolean.TRUE.equals(node.getBranch())) {
                    throw new RuntimeException("触发节点不能作为分支节点");
                }
            } else if (Boolean.TRUE.equals(node.getBranch())
                    && !"Boolean".equalsIgnoreCase(descriptor.getReturnType())
                    && !"boolean".equalsIgnoreCase(descriptor.getReturnType())) {
                throw new RuntimeException("分支节点 " + descriptor.getName() + " 的返回值不是 Boolean");
            }
            validateNodeInputs(node, descriptor);
        }

        Map<String, WorkflowNode> nodeMap = new HashMap<>();
        for (WorkflowNode node : definition.getNodes()) {
            nodeMap.put(node.getId(), node);
        }
        Set<String> edgeKeys = new HashSet<>();
        for (WorkflowEdge edge : definition.getEdges()) {
            if (!nodeMap.containsKey(edge.getFrom()) || !nodeMap.containsKey(edge.getTo())) {
                throw new RuntimeException("连线引用了不存在的节点：" + edge.getFrom() + " -> " + edge.getTo());
            }
            String edgeKey = edge.getFrom() + ":" + edge.getTo() + ":" + edge.getPort();
            if (!edgeKeys.add(edgeKey)) {
                throw new RuntimeException("重复连线：" + edgeKey);
            }
            if (!"success".equals(edge.getPort()) && !"failure".equals(edge.getPort())) {
                throw new RuntimeException("连线端口只能是 success/failure：" + edge.getPort());
            }
            if ("failure".equals(edge.getPort()) && !Boolean.TRUE.equals(nodeMap.get(edge.getFrom()).getBranch())) {
                throw new RuntimeException("普通节点不能有失败输出连线：" + edge.getFrom());
            }
            if (triggerNode != null && triggerNode.getId().equals(edge.getTo())) {
                throw new RuntimeException("触发节点不能作为连线目标：" + triggerNode.getId());
            }
        }
        checkNoCycle(definition);
        return computeTriggerKey(triggerNode);
    }

    /**
     * 校验任务节点每个参数的输入：非 nullable 参数必须有来源或默认值。
     */
    private void validateNodeInputs(WorkflowNode node, CallableDescriptor descriptor) {
        if (descriptor.getKind() == CallableDescriptor.CallableKind.TRIGGER) {
            return;
        }
        List<ParameterInfo> parameters = descriptor.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            final int paramIndex = i;
            ParameterInfo param = parameters.get(i);
            var input = node.getInputs().stream()
                    .filter(item -> Integer.valueOf(paramIndex).equals(item.getParamIndex()))
                    .findFirst()
                    .orElse(null);
            boolean hasValue = input != null
                    && ((input.getSource() != null && !input.getSource().isBlank())
                    || input.getDefaultValue() != null);
            if (!hasValue && !param.isNullable()) {
                throw new RuntimeException("节点 " + descriptor.getName() + " 的参数 "
                        + param.getName() + " 未配置数据来源或默认值");
            }
        }
    }

    /**
     * Kahn 拓扑排序检测 DAG 是否有环。
     */
    private void checkNoCycle(WorkflowDefinition definition) {
        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, List<String>> outgoing = new HashMap<>();
        for (WorkflowNode node : definition.getNodes()) {
            inDegree.put(node.getId(), 0);
            outgoing.put(node.getId(), new ArrayList<>());
        }
        for (WorkflowEdge edge : definition.getEdges()) {
            inDegree.merge(edge.getTo(), 1, Integer::sum);
            outgoing.get(edge.getFrom()).add(edge.getTo());
        }
        Queue<String> queue = new ArrayDeque<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }
        int visited = 0;
        while (!queue.isEmpty()) {
            String nodeId = queue.poll();
            visited++;
            for (String next : outgoing.get(nodeId)) {
                int remain = inDegree.get(next) - 1;
                inDegree.put(next, remain);
                if (remain == 0) {
                    queue.add(next);
                }
            }
        }
        if (visited != inDegree.size()) {
            throw new RuntimeException("工作流存在环，无法保存");
        }
    }

    /**
     * 根据触发节点计算注册键，例如 botEvent:{botQQ}:{事件key} 或 schedule:{cron}。
     */
    private String computeTriggerKey(WorkflowNode triggerNode) {
        if (triggerNode == null) {
            return "";
        }
        if (triggerNode.getCallable().startsWith("system:botEvent:")) {
            Object botQQ = triggerNode.getConfig() == null ? null : triggerNode.getConfig().get("botQQ");
            if (botQQ == null) {
                throw new RuntimeException("BOT 事件节点缺少 botQQ 配置");
            }
            String eventType = triggerNode.getCallable().substring("system:botEvent:".length());
            return "botEvent:" + botQQ + ":" + eventType;
        }
        if (CallableRegistry.SCHEDULE_KEY.equals(triggerNode.getCallable())) {
            Object cron = triggerNode.getConfig() == null ? null : triggerNode.getConfig().get("cronExpression");
            if (cron == null || cron.toString().isBlank()) {
                throw new RuntimeException("定时触发节点缺少 Cron 表达式配置");
            }
            String expression = cron.toString().trim();
            validateCronMinimumInterval(expression);
            return "schedule:" + expression;
        }
        return "";
    }

    /**
     * 校验 Cron 表达式语法，并强制两次执行间隔不少于 5 分钟。
     */
    private void validateCronMinimumInterval(String expression) {
        CronExpression cronExpression;
        try {
            cronExpression = CronExpression.parse(expression);
        } catch (Exception e) {
            throw new RuntimeException("Cron 表达式无效：" + expression);
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime first = cronExpression.next(now);
        LocalDateTime second = first == null ? null : cronExpression.next(first);
        LocalDateTime third = second == null ? null : cronExpression.next(second);
        if (first == null || second == null || third == null) {
            throw new RuntimeException("Cron 表达式没有可用的执行时间：" + expression);
        }
        long gap1 = Duration.between(first, second).toMinutes();
        long gap2 = Duration.between(second, third).toMinutes();
        if (gap1 < 5 || gap2 < 5) {
            throw new RuntimeException("定时任务最短执行间隔为 5 分钟：" + expression);
        }
    }

    /**
     * 判断工作流定义是否引用了指定插件。
     */
    private boolean usesPlugin(WorkflowDefinition definition, String pluginId) {
        if (definition == null || definition.getNodes() == null) {
            return false;
        }
        String prefix = "plugin:" + pluginId + ":";
        return definition.getNodes().stream().anyMatch(node -> node.getCallable() != null
                && node.getCallable().startsWith(prefix));
    }

    /**
     * 判断新旧定义是否存在执行逻辑差异。
     * 节点位置/画布视图不算逻辑；节点 callable、输入映射、默认值、分支、触发配置以及连线变化才算。
     */
    private boolean isExecutionLogicChanged(WorkflowDefinition oldDefinition, WorkflowDefinition newDefinition) {
        if (oldDefinition == null || newDefinition == null) {
            return oldDefinition != newDefinition;
        }
        return !extractNodeLogics(oldDefinition).equals(extractNodeLogics(newDefinition))
                || !extractEdgeLogics(oldDefinition).equals(extractEdgeLogics(newDefinition));
    }

    /**
     * 提取节点中与执行逻辑相关的字段，忽略 x/y 等纯画布信息。
     */
    private List<NodeLogic> extractNodeLogics(WorkflowDefinition definition) {
        List<NodeLogic> logics = new ArrayList<>();
        if (definition == null || definition.getNodes() == null) {
            return logics;
        }
        for (WorkflowNode node : definition.getNodes()) {
            List<InputLogic> inputs = new ArrayList<>();
            if (node.getInputs() != null) {
                for (ParamInput input : node.getInputs()) {
                    inputs.add(new InputLogic(input.getParamIndex(), input.getSource(), input.getDefaultValue()));
                }
            }
            inputs.sort(Comparator.comparing(InputLogic::paramIndex, Comparator.nullsFirst(Comparator.naturalOrder())));
            logics.add(new NodeLogic(
                    node.getId(),
                    node.getCallable(),
                    inputs,
                    Boolean.TRUE.equals(node.getBranch()),
                    node.getConfig() == null ? Map.of() : node.getConfig()));
        }
        logics.sort(Comparator.comparing(NodeLogic::id, Comparator.nullsFirst(Comparator.naturalOrder())));
        return logics;
    }

    /**
     * 提取连线逻辑，排序后比较，避免仅连线顺序变化触发误删。
     */
    private List<EdgeLogic> extractEdgeLogics(WorkflowDefinition definition) {
        List<EdgeLogic> logics = new ArrayList<>();
        if (definition == null || definition.getEdges() == null) {
            return logics;
        }
        for (WorkflowEdge edge : definition.getEdges()) {
            logics.add(new EdgeLogic(edge.getFrom(), edge.getTo(),
                    edge.getPort() == null ? "success" : edge.getPort()));
        }
        logics.sort(Comparator.comparing(EdgeLogic::from, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(EdgeLogic::to, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(EdgeLogic::port, Comparator.nullsFirst(Comparator.naturalOrder())));
        return logics;
    }

    /**
     * 节点执行逻辑快照。
     */
    private record NodeLogic(String id, String callable, List<InputLogic> inputs, boolean branch, Map<String, Object> config) {}

    /**
     * 单个参数输入快照。
     */
    private record InputLogic(Integer paramIndex, String source, Object defaultValue) {}

    /**
     * 连线逻辑快照。
     */
    private record EdgeLogic(String from, String to, String port) {}

    /**
     * 统计聚合值安全转 int。
     */
    private int toInt(Object value) {
        return value == null ? 0 : ((Number) value).intValue();
    }
}
