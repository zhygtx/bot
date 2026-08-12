package com.generalbot.workflow.service.impl;

import com.generalbot.workflow.entity.execution.WorkflowExecution;
import com.generalbot.workflow.mapper.WorkflowExecutionMapper;
import com.generalbot.workflow.service.WorkflowExecutionService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

/**
 * 工作流执行记录服务实现。
 */
@Service
public class WorkflowExecutionServiceImpl implements WorkflowExecutionService {

    private final WorkflowExecutionMapper executionMapper;

    /**
     * 构造执行记录服务。
     * @param executionMapper 执行记录 Mapper
     */
    public WorkflowExecutionServiceImpl(WorkflowExecutionMapper executionMapper) {
        this.executionMapper = executionMapper;
    }

    /**
     * 分页查询执行记录，列表不加载 trace，详情再按 ID 查询。
     */
    @Override
    public PageInfo<WorkflowExecution> findExecutions(String userId,
                                                      String workflowId,
                                                      String workflowName,
                                                      Long startTime,
                                                      Long endTime,
                                                      String sortField,
                                                      String sortOrder,
                                                      String status,
                                                      String keyword,
                                                      int pageNum,
                                                      int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(executionMapper.selectPage(
                userId, workflowId, workflowName, startTime, endTime, sortField, sortOrder, status, keyword));
    }

    /**
     * 根据 ID 查询完整执行记录，包含节点 trace。
     */
    @Override
    public WorkflowExecution findById(Long id) {
        return executionMapper.selectById(id);
    }
}
