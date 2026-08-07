package com.generalbot.workflow.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.generalbot.workflow.entity.execution.ExecutionTrace;
import com.generalbot.workflow.entity.execution.NodeTrace;
import com.generalbot.workflow.entity.execution.WorkflowExecution;
import com.generalbot.workflow.entity.WorkflowInfo;
import com.generalbot.workflow.entity.definition.WorkflowNode;
import com.generalbot.workflow.mapper.WorkflowExecutionMapper;

/**
 * JSON 快照式执行记录器：一次执行对应一行 workflow_execution。
 */
public class JsonExecutionRecorder implements ExecutionRecorder {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int MAX_TEXT_LENGTH = 64 * 1024;

    private final WorkflowExecutionMapper executionMapper;
    private final WorkflowExecution execution;
    private final ExecutionTrace trace = new ExecutionTrace();

    /**
     * 初始化记录器，预填工作流摘要信息。
     */
    public JsonExecutionRecorder(WorkflowExecutionMapper executionMapper,
                                 WorkflowInfo workflowInfo,
                                 String triggerKey) {
        this.executionMapper = executionMapper;
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
        nodeTrace.setInput(truncate(input));
        nodeTrace.setOutput(truncate(output));
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
        nodeTrace.setInput(truncate(input));
        nodeTrace.setError(truncateText(errorMessage(error)));
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
        execution.setErrorMessage(truncateText(errorMessage(error)));
        finish(endTime);
    }

    /**
     * 持久化执行记录并返回自增 ID。
     */
    @Override
    public Long save() {
        executionMapper.insert(execution);
        return execution.getId();
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
     * 输入输出统一序列化为 JSON 并截断，避免超大文本撑爆执行记录。
     */
    private Object truncate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String text) {
            return truncateText(text);
        }
        try {
            String json = MAPPER.writeValueAsString(value);
            return truncateText(json);
        } catch (Exception e) {
            return truncateText(value.toString());
        }
    }

    /**
     * 超过阈值时截断字符串。
     */
    private String truncateText(String text) {
        if (text == null) {
            return null;
        }
        return text.length() > MAX_TEXT_LENGTH
                ? text.substring(0, MAX_TEXT_LENGTH) + "...[已截断]"
                : text;
    }

    /**
     * 提取可读的错误信息，优先取真实异常 cause。
     */
    private String errorMessage(Throwable error) {
        if (error == null) {
            return "";
        }
        Throwable cause = error.getCause() != null ? error.getCause() : error;
        String message = cause.getMessage();
        return message == null || message.isBlank() ? cause.getClass().getName() : message;
    }
}
