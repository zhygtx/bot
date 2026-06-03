package com.example.demo.service.impl;

import com.example.demo.mapper.log.NodeLogMapper;
import com.example.demo.mapper.log.WorkflowLogMapper;
import com.example.demo.pojo.log.NodeLog;
import com.example.demo.pojo.log.WorkflowLog;
import com.example.demo.service.WorkflowLogService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * @return 添加结果
     */
    @Override
    @Transactional
    public int add(WorkflowLog workflowLog, List<NodeLog> nodeLogs) {
        int workflowLogResult = workflowLogMapper.insert(workflowLog);
        int nodeLogResult = nodeLogMapper.insert(nodeLogs);
        return workflowLogResult + nodeLogResult;
    }

    /**
     * 根据用户ID查询工作流日志
     *
     * @param userId 用户ID
     * @return 工作流日志列表
     */
    @Override
    public PageInfo<WorkflowLog> findWorkflowLogs(String userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(workflowLogMapper.selectByUserId(userId));
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
