package com.example.demo.service.impl;

import com.example.demo.mapper.workflow.*;
import com.example.demo.pojo.workflow.WorkflowInfo;
import com.example.demo.service.WorkflowService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

/**
 * 工作流服务实现类
 */
@Service
public class WorkflowServiceImpl implements WorkflowService {

    final
    WorkflowInfoMapper workflowInfoMapper;
    final
    NodeDefaultsMapper nodeDefaultsMapper;
    final
    NodeNextRelationMapper nodeNextRelationMapper;
    final
    NodePreRelationMapper nodePreRelationMapper;
    final
    NodeMapper nodeMapper;
    final
    ConditionMapper conditionMapper;
    final
    DataMapMapper dataMapMapper;

    public WorkflowServiceImpl(WorkflowInfoMapper workflowInfoMapper, NodeDefaultsMapper nodeDefaultsMapper, NodeNextRelationMapper nodeNextRelationMapper, NodePreRelationMapper nodePreRelationMapper, NodeMapper nodeMapper, ConditionMapper conditionMapper, DataMapMapper dataMapMapper) {
        this.workflowInfoMapper = workflowInfoMapper;
        this.nodeDefaultsMapper = nodeDefaultsMapper;
        this.nodeNextRelationMapper = nodeNextRelationMapper;
        this.nodePreRelationMapper = nodePreRelationMapper;
        this.nodeMapper = nodeMapper;
        this.conditionMapper = conditionMapper;
        this.dataMapMapper = dataMapMapper;
    }

    /**
     * 添加工作流
     * @param workflowInfo 工作流信息
     * @return 添加结果
     */
    @Override
    public int add(WorkflowInfo workflowInfo) {
        return 0;
    }

    /**
     * 删除工作流
     * @param id 工作流ID
     * @return 删除结果
     */
    @Override
    public int remove(String id) {
        return workflowInfoMapper.deleteById(id);
    }

    /**
     * 查询所有工作流
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 工作流列表
     */
    @Override
    public PageInfo<WorkflowInfo> findAll(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(workflowInfoMapper.getAll());
    }
}
