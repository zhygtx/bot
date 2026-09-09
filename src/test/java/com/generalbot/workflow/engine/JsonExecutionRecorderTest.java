package com.generalbot.workflow.engine;

import com.generalbot.workflow.entity.WorkflowInfo;
import com.generalbot.workflow.entity.definition.WorkflowDefinition;
import com.generalbot.workflow.entity.definition.WorkflowNode;
import com.generalbot.workflow.entity.execution.BigText;
import com.generalbot.workflow.entity.execution.NodeTrace;
import com.generalbot.workflow.entity.execution.WorkflowExecution;
import com.generalbot.workflow.mapper.BigTextMapper;
import com.generalbot.workflow.mapper.WorkflowExecutionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * JsonExecutionRecorder 大文本离线存储测试。
 */
class JsonExecutionRecorderTest {

    private WorkflowExecutionMapper executionMapper;
    private BigTextMapper bigTextMapper;
    private JsonExecutionRecorder recorder;

    @BeforeEach
    void setUp() {
        executionMapper = mock(WorkflowExecutionMapper.class);
        bigTextMapper = mock(BigTextMapper.class);

        WorkflowNode node = new WorkflowNode();
        node.setId("node_1");

        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setNodes(List.of(node));

        WorkflowInfo workflowInfo = new WorkflowInfo();
        workflowInfo.setId("wf_1");
        workflowInfo.setUserId("u_1");
        workflowInfo.setName("测试工作流");
        workflowInfo.setDefinition(definition);

        recorder = new JsonExecutionRecorder(executionMapper, bigTextMapper, workflowInfo, "test");
    }

    @Test
    void largeOutputIsOffloadedToBigTextAndReferencedInTrace() {
        WorkflowNode node = node("node_1");
        CallableDescriptor descriptor = CallableDescriptor.builder()
                .name("发送消息")
                .kind(CallableDescriptor.CallableKind.TASK)
                .build();
        String largeOutput = "x".repeat(11 * 1024);

        recorder.workflowStarted(null, "test", 0, null);
        recorder.nodeStarted(node, descriptor, 0);
        recorder.nodeCompleted(node, 10, "smallInput", largeOutput);
        recorder.workflowCompleted(20);

        recorder.save();
        ArgumentCaptor<WorkflowExecution> executionCaptor = ArgumentCaptor.forClass(WorkflowExecution.class);
        verify(executionMapper).insert(executionCaptor.capture());
        NodeTrace trace = executionCaptor.getValue().getTrace().getNodes().get(0);
        assertEquals("smallInput", trace.getInput());
        assertTrue(trace.getOutput() instanceof String);
        assertTrue(((String) trace.getOutput()).startsWith(JsonExecutionRecorder.BIG_TEXT_PREFIX));

        ArgumentCaptor<List<BigText>> bigTextCaptor = ArgumentCaptor.forClass(List.class);
        verify(bigTextMapper).insertBatch(bigTextCaptor.capture());
        assertEquals(1, bigTextCaptor.getValue().size());
        assertEquals(largeOutput, bigTextCaptor.getValue().get(0).getValue());
        assertEquals(trace.getOutput(), bigTextCaptor.getValue().get(0).getKey());
    }

    @Test
    void smallOutputStaysInline() {
        WorkflowNode node = node("node_1");
        CallableDescriptor descriptor = CallableDescriptor.builder()
                .name("发送消息")
                .kind(CallableDescriptor.CallableKind.TASK)
                .build();

        recorder.workflowStarted(null, "test", 0, null);
        recorder.nodeStarted(node, descriptor, 0);
        recorder.nodeCompleted(node, 10, "smallInput", "smallOutput");
        recorder.workflowCompleted(20);
        recorder.save();

        ArgumentCaptor<WorkflowExecution> executionCaptor = ArgumentCaptor.forClass(WorkflowExecution.class);
        verify(executionMapper).insert(executionCaptor.capture());
        NodeTrace trace = executionCaptor.getValue().getTrace().getNodes().get(0);
        assertEquals("smallOutput", trace.getOutput());
    }

    @Test
    void failureRecordsBriefReasonAndFullStack() {
        WorkflowNode node = node("node_1");
        CallableDescriptor descriptor = CallableDescriptor.builder()
                .name("发送消息")
                .kind(CallableDescriptor.CallableKind.TASK)
                .build();
        RuntimeException error = new RuntimeException(
                "节点 发送消息 执行失败：com/mikuac/shiro/common/utils/MsgUtils",
                new NoClassDefFoundError("com/mikuac/shiro/common/utils/MsgUtils"));

        recorder.workflowStarted(null, "test", 0, null);
        recorder.nodeStarted(node, descriptor, 0);
        recorder.nodeFailed(node, 10, "smallInput", error);
        recorder.workflowFailed(20, error);
        recorder.save();

        ArgumentCaptor<WorkflowExecution> executionCaptor = ArgumentCaptor.forClass(WorkflowExecution.class);
        verify(executionMapper).insert(executionCaptor.capture());
        WorkflowExecution execution = executionCaptor.getValue();
        assertEquals("节点 发送消息 执行失败：com/mikuac/shiro/common/utils/MsgUtils", execution.getErrorMessage());
        NodeTrace failedTrace = execution.getTrace().getNodes().get(0);
        assertTrue(failedTrace.getError().contains("Caused by: java.lang.NoClassDefFoundError"));
        assertTrue(failedTrace.getError().contains("com/mikuac/shiro/common/utils/MsgUtils"));
    }

    private WorkflowNode node(String id) {
        WorkflowNode node = new WorkflowNode();
        node.setId(id);
        return node;
    }
}
