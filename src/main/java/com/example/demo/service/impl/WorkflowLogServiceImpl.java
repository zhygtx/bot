package com.example.demo.service.impl;

import com.example.demo.mapper.log.NodeLogMapper;
import com.example.demo.mapper.log.WorkflowLogMapper;
import com.example.demo.pojo.entity.log.NodeLog;
import com.example.demo.pojo.entity.log.WorkflowLog;
import com.example.demo.service.WorkflowLogService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 工作流日志服务实现类
 */
@Service
public class WorkflowLogServiceImpl implements WorkflowLogService {

    private final WorkflowLogMapper workflowLogMapper;
    private final NodeLogMapper nodeLogMapper;

    public WorkflowLogServiceImpl(WorkflowLogMapper workflowLogMapper, NodeLogMapper nodeLogMapper) {
        this.workflowLogMapper = workflowLogMapper;
        this.nodeLogMapper = nodeLogMapper;
    }

    /**
     * 添加工作流日志
     *
     * @param workflowLog 工作流日志
     * @param nodeLogs    节点日志列表
     */
    @Override
    @Async
    public void add(WorkflowLog workflowLog, List<NodeLog> nodeLogs) {
        workflowLogMapper.insert(workflowLog);
        nodeLogMapper.insert(nodeLogs);
    }

    /**
     * 根据用户ID、工作流名称和时间范围查询工作流日志
     *
     * @param userId 用户ID
     * @param workflowName 工作流名称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 工作流日志列表
     */
    @Override
    public PageInfo<WorkflowLog> findWorkflowLogs(String userId, String workflowName, Long startTime, Long endTime, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(workflowLogMapper.selectByCondition(userId, workflowName, startTime, endTime));
    }

    /**
     * 根据工作流日志ID查询节点日志
     *
     * @param workflowLogId 工作流日志ID
     * @return 节点日志列表
     */
    @Override
    public List<NodeLog> findNodeLogs(String workflowLogId) {
        return nodeLogMapper.selectByWorkflowLogId(workflowLogId);
    }
}
