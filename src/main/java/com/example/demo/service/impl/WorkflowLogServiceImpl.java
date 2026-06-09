package com.example.demo.service.impl;

import com.example.demo.mapper.log.BigTextMapper;
import com.example.demo.mapper.log.NodeLogMapper;
import com.example.demo.mapper.log.WorkflowLogMapper;
import com.example.demo.pojo.entity.log.BigText;
import com.example.demo.pojo.entity.log.NodeLog;
import com.example.demo.pojo.entity.log.WorkflowLog;
import com.example.demo.service.WorkflowLogService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.scheduling.annotation.Async;
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
    private final BigTextMapper bigTextMapper;

    public WorkflowLogServiceImpl(WorkflowLogMapper workflowLogMapper, NodeLogMapper nodeLogMapper, BigTextMapper bigTextMapper) {
        this.workflowLogMapper = workflowLogMapper;
        this.nodeLogMapper = nodeLogMapper;
        this.bigTextMapper = bigTextMapper;
    }

    /**
     * 添加工作流日志
     */
    @Override
    @Async
    @Transactional
    public void add(WorkflowLog workflowLog, List<NodeLog> nodeLogs, List<BigText> bigTextList) {
        workflowLogMapper.insert(workflowLog);
        Long workflowLogId = workflowLogMapper.getLastInsertId();
        for (NodeLog nodeLog : nodeLogs) {
            nodeLog.setWorkflowLogId(workflowLogId);
        }
        nodeLogMapper.insert(nodeLogs);
        if (bigTextList != null && !bigTextList.isEmpty()) {
            bigTextMapper.insertBatch(bigTextList);
        }
    }

    /**
     * 根据条件查询工作流日志
     */
    @Override
    public PageInfo<WorkflowLog> findWorkflowLogs(String userId, String workflowName, Long startTime, Long endTime, String sortField, String sortOrder, String status, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(workflowLogMapper.selectByCondition(userId, workflowName, startTime, endTime, sortField, sortOrder, status));
    }

    /**
     * 根据工作流日志ID查询节点日志
     */
    @Override
    public List<NodeLog> findNodeLogs(Long workflowLogId) {
        return nodeLogMapper.selectByWorkflowLogId(workflowLogId);
    }

    @Override
    public String findBigText(String key) {
        return bigTextMapper.selectByKey(key);
    }
}