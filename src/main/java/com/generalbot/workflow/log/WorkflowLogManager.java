package com.generalbot.workflow.log;

import com.generalbot.common.context.ThreadLocalManager;
import com.generalbot.workflow.entity.log.BigText;
import com.generalbot.workflow.entity.log.NodeLog;
import com.generalbot.workflow.entity.log.WorkflowLog;
import com.generalbot.workflow.service.WorkflowLogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 工作流日志管理器
 * 负责日志对象的构建、BigText 处理及日志持久化，与执行逻辑完全解耦
 */
@Slf4j
@Component
public class WorkflowLogManager {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final WorkflowLogService workflowLogService;

    public WorkflowLogManager(WorkflowLogService workflowLogService) {
        this.workflowLogService = workflowLogService;
    }

    // ==================== WorkflowLog 构建 ====================

    /**
     * 创建工作流日志并绑定到当前线程
     */
    public void createWorkflowLog(String workflowId, String userId, String workflowName,
                                  int expectedNodeCount, Object initialContext, long startTime) {
        String initialContextStr = null;
        if (initialContext != null) {
            initialContextStr = toJson(initialContext);
        }

        WorkflowLog workflowLog = WorkflowLog.builder()
                .workflowId(workflowId)
                .userId(userId)
                .workflowName(workflowName)
                .expectedNodeCount(expectedNodeCount)
                .startTime(startTime)
                .initialContext(initialContextStr)
                .isError(false)
                .build();
        ThreadLocalManager.setWorkflowLog(workflowLog);
    }

    // ==================== NodeLog 构建 ====================

    /**
     * 创建节点日志并注册到当前线程
     */
    public void createNodeLog(String nodeId, String methodId, int order,
                              String methodName, String methodDescription) {
        NodeLog nodeLog = NodeLog.builder()
                .nodeId(nodeId)
                .methodId(methodId)
                .order(order)
                .methodName(methodName)
                .methodDescription(methodDescription)
                .isError(false)
                .build();
        ThreadLocalManager.addNodeLog(nodeLog);
    }

    /**
     * 记录节点输入输出（含 BigText 处理）
     */
    public void recordNodeInputOutput(String nodeId, Map<String, Object> parameters, Object result) {
        NodeLog nodeLog = ThreadLocalManager.getNodeLog(nodeId);
        if (nodeLog == null) {
            return;
        }
        nodeLog.setInput(toJsonOrBigText(parameters, nodeId));
        nodeLog.setOutput(toJsonOrBigText(result, nodeId));
        ThreadLocalManager.addNodeLog(nodeLog);
    }

    /**
     * 记录节点执行成功（设置耗时）
     */
    public void recordNodeSuccess(String nodeId, long elapsedMs) {
        NodeLog nodeLog = ThreadLocalManager.getNodeLog(nodeId);
        if (nodeLog != null) {
            nodeLog.setExecutionTime(elapsedMs);
            ThreadLocalManager.addNodeLog(nodeLog);
        }
    }

    /**
     * 记录节点执行失败（设置错误信息和耗时）
     */
    public void recordNodeError(String nodeId, long elapsedMs, Throwable error) {
        NodeLog nodeLog = ThreadLocalManager.getNodeLog(nodeId);
        if (nodeLog == null) {
            return;
        }

        String errorKey = saveErrorAsBigText(nodeId, error);
        nodeLog.setOutput(errorKey);
        nodeLog.setIsError(true);
        nodeLog.setExecutionTime(elapsedMs);
        ThreadLocalManager.addNodeLog(nodeLog);

        // 同步标记工作流日志为错误
        WorkflowLog wfLog = ThreadLocalManager.getWorkflowLog();
        if (wfLog != null) {
            wfLog.setIsError(true);
        }
    }

    // ==================== 日志持久化 ====================

    /**
     * 完成工作流日志并持久化
     */
    public void finalizeAndSave(long workflowStartTime) {
        WorkflowLog workflowLog = ThreadLocalManager.getWorkflowLog();
        if (workflowLog == null) {
            return;
        }

        workflowLog.setExecutionTime(System.currentTimeMillis() - workflowStartTime);
        workflowLog.setActualNodeCount(ThreadLocalManager.getNodeLogList().size());

        workflowLogService.add(workflowLog, ThreadLocalManager.getNodeLogList(), getBigTextList());
        log.debug("工作流日志保存完成");
    }

    /**
     * 异常情况下完成工作流日志并持久化
     */
    public void finalizeAndSaveWithError(long workflowStartTime, Throwable error) {
        WorkflowLog workflowLog = ThreadLocalManager.getWorkflowLog();
        if (workflowLog == null) {
            return;
        }

        workflowLog.setExecutionTime(System.currentTimeMillis() - workflowStartTime);
        workflowLog.setActualNodeCount(ThreadLocalManager.getNodeLogList().size());
        workflowLog.setIsError(true);

        String errorKey = saveErrorAsBigText(workflowLog.getWorkflowId(), error);
        workflowLog.setErrorLog(errorKey);

        try {
            workflowLogService.add(workflowLog, ThreadLocalManager.getNodeLogList(), getBigTextList());
            log.debug("异常情况下工作流日志保存完成");
        } catch (Exception e) {
            log.error("保存工作流日志失败", e);
        }
    }

    // ==================== BigText 处理 ====================

    /**
     * 将对象序列化为 JSON，超过阈值则转为 BigText 引用
     */
    public String toJsonOrBigText(Object data, String nodeId) {
        if (data == null) {
            return null;
        }
        String json = toJson(data);
        return handleBigText(json, nodeId);
    }

    /**
     * 将异常的完整堆栈保存为 BigText
     */
    private String saveErrorAsBigText(String nodeId, Throwable error) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        error.printStackTrace(pw);
        return handleBigText(sw.toString(), nodeId);
    }

    /**
     * BigText 阈值判断：超过阈值生成引用 key，否则原样返回
     */
    private String handleBigText(String data, String nodeId) {
        if (data == null || data.length() <= ThreadLocalManager.BIG_TEXT_THRESHOLD) {
            return data;
        }
        String key = ThreadLocalManager.BIG_TEXT_PREFIX + nodeId + ":"
                + System.currentTimeMillis() + ":"
                + UUID.randomUUID().toString().substring(0, 8);
        ThreadLocalManager.addBigText(key, data);
        return key;
    }

    /**
     * 从 ThreadLocal 收集所有 BigText
     */
    private List<BigText> getBigTextList() {
        Map<String, String> cache = ThreadLocalManager.getBigTextCache();
        if (cache == null || cache.isEmpty()) {
            return Collections.emptyList();
        }
        return cache.entrySet().stream()
                .map(entry -> new BigText(entry.getKey(), entry.getValue(), null))
                .collect(Collectors.toList());
    }

    // ==================== 工具方法 ====================

    private String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return obj.toString();
        }
    }
}
