package com.example.demo.util;

import com.example.demo.pojo.entity.log.NodeLog;
import com.example.demo.pojo.entity.log.WorkflowLog;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ThreadLocal管理工具类，用于安全管理ThreadLocal变量
 */
@Slf4j
public class ThreadLocalManager {
    // 方法实例缓存
    private static final ThreadLocal<Map<String, Object>> methodInstanceCache = 
        ThreadLocal.withInitial(HashMap::new);
    
    // 执行上下文缓存
    private static final ThreadLocal<Map<String, Object>> executionContext = 
        ThreadLocal.withInitial(HashMap::new);

    // 工作流日志缓存
    private static final ThreadLocal<WorkflowLog> workflowLog =
            ThreadLocal.withInitial(WorkflowLog::new);

    // 节点日志缓存
    private static final ThreadLocal<Map<String, NodeLog>> nodeLogList =
            ThreadLocal.withInitial(HashMap::new);

    // 在ThreadLocalManager类中添加
    private static final String USER_ID_KEY = "userId";
    private static final String PLUGIN_ID_KEY  = "pluginId";

    /**
     * 设置用户ID
     * @param userId 用户ID
     */
    public static void setUserId(String userId) {
        getExecutionContext().put(USER_ID_KEY, userId);
    }

    /**
     * 获取用户ID
     * @return 用户ID
     */
    public static String getUserId() {
        return (String) getExecutionContext().get(USER_ID_KEY);
    }

    /**
     * 设置插件ID
     * @param pluginId 插件ID
     */
    public static void setPluginId(String pluginId) {
        getExecutionContext().put(PLUGIN_ID_KEY, pluginId);
    }

    /**
     * 获取插件ID
     * @return 插件ID
     */
    public static String getPluginId() {
        return (String) getExecutionContext().get(PLUGIN_ID_KEY);
    }
    
    /**
     * 获取方法实例缓存
     * @return 方法实例缓存
     */
    public static Map<String, Object> getMethodInstanceCache() {
        return methodInstanceCache.get();
    }
    
    /**
     * 获取执行上下文
     * @return 执行上下文
     */
    public static Map<String, Object> getExecutionContext() {
        return executionContext.get();
    }

    /**
     * 获取工作流日志
     * @return 工作流日志
     */
    public static WorkflowLog getWorkflowLog() {
        return workflowLog.get();
    }

    /**
     * 设置工作流日志
     * @param workflowLog 工作流日志
     */
    public static void setWorkflowLog(WorkflowLog workflowLog) {
        ThreadLocalManager.workflowLog.set(workflowLog);
    }

    /**
     * 获取节点日志列表
     * @return 节点日志列表
     */
    public static List<NodeLog> getNodeLogList() {
        return new ArrayList<>(nodeLogList.get().values());
    }

    /**
     * 获取节点日志
     * @param nodeId 节点ID
     * @return 节点日志
     */
    public static NodeLog getNodeLog(String nodeId) {
        return nodeLogList.get().get(nodeId);
    }

    /**
     * 添加节点日志
     * @param nodeLog 节点日志
     */
    public static void addNodeLog(NodeLog nodeLog) {
        nodeLogList.get().put(nodeLog.getNodeId(), nodeLog);
    }
    
    /**
     * 清理所有ThreadLocal变量
     */
    public static void clear() {
        methodInstanceCache.remove();
        executionContext.remove();
        workflowLog.remove();
        nodeLogList.remove();
    }
}
