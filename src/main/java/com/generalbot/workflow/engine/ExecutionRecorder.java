package com.generalbot.workflow.engine;

import com.generalbot.workflow.entity.WorkflowInfo;
import com.generalbot.workflow.entity.definition.WorkflowNode;

/**
 * 执行记录器接口。引擎只依赖该接口，未来换成事件式存储时只需新增实现。
 */
public interface ExecutionRecorder {

    /**
     * 工作流开始
     * @param workflowInfo 工作流信息
     * @param triggerKey 触发键
     * @param startTime 开始时间
     * @param initialContext 初始上下文
     */
    void workflowStarted(WorkflowInfo workflowInfo, String triggerKey, long startTime, Object initialContext);

    /**
     * 节点开始
     * @param node 节点
     * @param descriptor callable 描述
     * @param startTime 开始时间
     */
    void nodeStarted(WorkflowNode node, CallableDescriptor descriptor, long startTime);

    /**
     * 节点成功
     * @param node 节点
     * @param endTime 结束时间
     * @param input 输入
     * @param output 输出
     */
    void nodeCompleted(WorkflowNode node, long endTime, Object input, Object output);

    /**
     * 节点失败
     * @param node 节点
     * @param endTime 结束时间
     * @param input 输入
     * @param error 异常
     */
    void nodeFailed(WorkflowNode node, long endTime, Object input, Throwable error);

    /**
     * 工作流成功结束
     * @param endTime 结束时间
     */
    void workflowCompleted(long endTime);

    /**
     * 工作流失败结束
     * @param endTime 结束时间
     * @param error 异常
     */
    void workflowFailed(long endTime, Throwable error);

    /**
     * 持久化并返回执行记录ID
     * @return 执行记录ID
     */
    Long save();
}
