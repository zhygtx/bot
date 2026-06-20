package com.example.demo.service;

import com.example.demo.pojo.entity.log.BigText;
import com.example.demo.pojo.entity.log.NodeLog;
import com.example.demo.pojo.entity.log.WorkflowLog;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * 工作流日志服务接口
 */
public interface WorkflowLogService {

    void add(WorkflowLog workflowLog, List<NodeLog> nodeLogs, List<BigText> bigTextList);

    PageInfo<WorkflowLog> findWorkflowLogs(String userId, String workflowId, String workflowName, Long startTime, Long endTime, String sortField, String sortOrder, String status, int pageNum, int pageSize);

    List<NodeLog> findNodeLogs(Long workflowLogId);

    String findBigText(String key);

    /**
     * 根据工作流日志ID获取工作流日志
     * @param workflowLogId 工作流日志ID
     * @return 工作流日志
     */
    WorkflowLog findWorkflowLogById(Long workflowLogId);
}