package com.example.demo.service;

import com.example.demo.pojo.log.NodeLog;
import com.example.demo.pojo.log.WorkflowLog;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * 工作流日志服务接口
 */
public interface WorkflowLogService {

    int add(WorkflowLog workflowLog, List<NodeLog> nodeLogs);

    PageInfo<WorkflowLog> findWorkflowLogs(String userId, int pageNum, int pageSize);

    List<NodeLog> findNodeLogs(String workflowLogId);
}
