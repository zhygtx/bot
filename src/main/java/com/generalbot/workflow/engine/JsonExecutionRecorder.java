package com.generalbot.workflow.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.generalbot.common.util.ExceptionUtils;
import com.generalbot.workflow.entity.execution.BigText;
import com.generalbot.workflow.entity.execution.ExecutionTrace;
import com.generalbot.workflow.entity.execution.NodeTrace;
import com.generalbot.workflow.entity.execution.WorkflowExecution;
import com.generalbot.workflow.entity.WorkflowInfo;
import com.generalbot.workflow.entity.definition.WorkflowNode;
import com.generalbot.workflow.mapper.BigTextMapper;
import com.generalbot.workflow.mapper.WorkflowExecutionMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * JSON 快照式执行记录器：一次执行对应一行 workflow_execution。
 */
@Slf4j
public class JsonExecutionRecorder implements ExecutionRecorder {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 超过该阈值的文本不再内嵌，而是写入 big_text 表并按引用键返回。
     */
    private static final int BIG_TEXT_THRESHOLD = 10 * 1024;

    /**
     * 引用键前缀，前端据此识别并懒加载内容。
     */
    public static final String BIG_TEXT_PREFIX = "BIG_TEXT:";

    private final WorkflowExecutionMapper executionMapper;
    private final BigTextMapper bigTextMapper;
    private final WorkflowExecution execution;
    private final ExecutionTrace trace = new ExecutionTrace();

    /**
     * 本次执行期间被离线存储的大数据缓存（key -> 完整内容）。
     */
    private final Map<String, String> bigTextCache = new HashMap<>();

    /**
     * 初始化记录器，预填工作流摘要信息。
     */
    public JsonExecutionRecorder(WorkflowExecutionMapper executionMapper,
                                 BigTextMapper bigTextMapper,
                                 WorkflowInfo workflowInfo,
                                 String triggerKey) {
        this.executionMapper = executionMapper;
        this.bigTextMapper = bigTextMapper;
        this.execution = new WorkflowExecution();
        this.execution.setWorkflowId(workflowInfo.getId());
        this.execution.setUserId(workflowInfo.getUserId());
        this.execution.setWorkflowName(workflowInfo.getName());
        this.execution.setTriggerKey(triggerKey);
        this.execution.setExpectedNodeCount(workflowInfo.getDefinition() == null
                ? 0 : workflowInfo.getDefinition().getNodes().size());
        this.execution.setTrace(trace);
    }

    /**
     * 记录工作流开始时间。
     */
    @Override
    public void workflowStarted(WorkflowInfo workflowInfo, String triggerKey, long startTime, Object initialContext) {
        execution.setStartTime(startTime);
    }

    /**
     * 追加节点开始轨迹。
     */
    @Override
    public void nodeStarted(WorkflowNode node, CallableDescriptor descriptor, long startTime) {
        NodeTrace nodeTrace = new NodeTrace();
        nodeTrace.setNodeId(node.getId());
        nodeTrace.setCallable(node.getCallable());
        nodeTrace.setName(descriptor.getName());
        nodeTrace.setStatus("RUNNING");
        nodeTrace.setStartTime(startTime);
        trace.getNodes().add(nodeTrace);
    }

    /**
     * 更新节点成功状态与输入输出。
     */
    @Override
    public void nodeCompleted(WorkflowNode node, long endTime, Object input, Object output) {
        NodeTrace nodeTrace = findTrace(node.getId());
        if (nodeTrace == null) {
            return;
        }
        nodeTrace.setStatus("SUCCESS");
        nodeTrace.setEndTime(endTime);
        nodeTrace.setInput(store(input));
        nodeTrace.setOutput(store(output));
    }

    /**
     * 更新节点失败状态与错误信息。
     */
    @Override
    public void nodeFailed(WorkflowNode node, long endTime, Object input, Throwable error) {
        NodeTrace nodeTrace = findTrace(node.getId());
        if (nodeTrace == null) {
            return;
        }
        nodeTrace.setStatus("FAILED");
        nodeTrace.setEndTime(endTime);
        nodeTrace.setInput(store(input));
        nodeTrace.setError(storeText(ExceptionUtils.fullStackTrace(error)));
    }

    /**
     * 标记工作流成功结束。
     */
    @Override
    public void workflowCompleted(long endTime) {
        execution.setStatus("SUCCESS");
        finish(endTime);
    }

    /**
     * 标记工作流失败结束并记录错误信息。
     */
    @Override
    public void workflowFailed(long endTime, Throwable error) {
        execution.setStatus("FAILED");
        execution.setErrorMessage(storeText(ExceptionUtils.brief(error)));
        finish(endTime);
    }

    /**
     * 持久化执行记录并返回自增 ID。
     */
    @Override
    public Long save() {
        executionMapper.insert(execution);
        if (!bigTextCache.isEmpty()) {
            persistBigTextCache();
        }
        return execution.getId();
    }

    /**
     * 分批写入大数据缓存，避免单条超大 INSERT 超出数据库包大小限制。
     */
    private void persistBigTextCache() {
        List<BigText> entries = new ArrayList<>(bigTextCache.size());
        bigTextCache.forEach((key, value) -> entries.add(new BigText(key, execution.getId(), value)));
        int batchSize = 10;
        for (int i = 0; i < entries.size(); i += batchSize) {
            List<BigText> batch = entries.subList(i, Math.min(i + batchSize, entries.size()));
            try {
                bigTextMapper.insertBatch(new ArrayList<>(batch));
            } catch (Exception e) {
                // 大数据写入失败不应掩盖执行记录本身的保存结果，交由日志排查。
                log.error("工作流执行记录 {} 的大数据写入失败（第 {} 批，共 {} 条）",
                        execution.getId(), i / batchSize + 1, batch.size(), e);
            }
        }
    }

    /**
     * 统一填充结束时间、耗时和实际节点数。
     */
    private void finish(long endTime) {
        execution.setEndTime(endTime);
        execution.setDurationMs(endTime - execution.getStartTime());
        execution.setActualNodeCount(trace.getNodes().size());
    }

    /**
     * 按节点 ID 查找轨迹记录。
     */
    private NodeTrace findTrace(String nodeId) {
        return trace.getNodes().stream()
                .filter(t -> nodeId.equals(t.getNodeId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 输入输出统一序列化为 JSON，超过阈值时离线存储到 big_text。
     */
    private Object store(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String text) {
            return storeText(text);
        }
        try {
            String json = MAPPER.writeValueAsString(value);
            return storeText(json);
        } catch (Exception e) {
            return storeText(value.toString());
        }
    }

    /**
     * 文本超过阈值时写入 big_text 并返回引用键，否则原样返回。
     */
    private String storeText(String text) {
        if (text == null) {
            return null;
        }
        if (text.length() <= BIG_TEXT_THRESHOLD) {
            return text;
        }
        String key = BIG_TEXT_PREFIX + UUID.randomUUID() + ":" + System.currentTimeMillis();
        bigTextCache.put(key, text);
        return key;
    }

}
