package com.generalbot.workflow.service.impl;

import com.generalbot.workflow.mapper.log.BigTextMapper;
import com.generalbot.workflow.mapper.log.NodeLogMapper;
import com.generalbot.workflow.mapper.log.WorkflowLogMapper;
import com.generalbot.workflow.entity.log.BigText;
import com.generalbot.workflow.entity.log.NodeLog;
import com.generalbot.workflow.entity.log.WorkflowLog;
import com.generalbot.workflow.service.WorkflowLogService;
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
    @Transactional
    public void add(WorkflowLog workflowLog, List<NodeLog> nodeLogs, List<BigText> bigTextList) {
        workflowLogMapper.insert(workflowLog);
        Long workflowLogId = workflowLogMapper.getLastInsertId();
        workflowLog.setId(workflowLogId);
        for (NodeLog nodeLog : nodeLogs) {
            nodeLog.setWorkflowLogId(workflowLogId);
        }
        nodeLogMapper.insert(nodeLogs);
        if (bigTextList != null && !bigTextList.isEmpty()) {
            bigTextList.forEach(bigText -> bigText.setWorkflowLogId(workflowLogId));
            bigTextMapper.insertBatch(bigTextList);
        }
    }

    /**
     * 根据条件查询工作流日志
     */
    @Override
    public PageInfo<WorkflowLog> findWorkflowLogs(String userId, String workflowId, String workflowName, Long startTime, Long endTime, String sortField, String sortOrder, String status, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(workflowLogMapper.selectByCondition(userId, workflowId, workflowName, startTime, endTime, sortField, sortOrder, status));
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

    @Override
    public WorkflowLog findWorkflowLogById(Long workflowLogId) {
        return workflowLogMapper.selectById(workflowLogId);
    }
}
