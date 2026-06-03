package com.example.demo.service;

import com.example.demo.pojo.entity.log.NodeLog;
import com.example.demo.pojo.entity.log.WorkflowLog;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * 工作流日志服务接口
 */
public interface WorkflowLogService {

    void add(WorkflowLog workflowLog, List<NodeLog> nodeLogs);

    PageInfo<WorkflowLog> findWorkflowLogs(String userId, String workflowName, Long startTime, Long endTime, int pageNum, int pageSize);

    List<NodeLog> findNodeLogs(String workflowLogId);
}
