package com.example.demo.service.impl;

import com.example.demo.mapper.workflow.*;
import com.example.demo.pojo.workflow.*;
import com.example.demo.service.WorkflowService;
import com.example.demo.util.WorkflowUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 工作流服务实现类
 */
@Service
@Slf4j
public class WorkflowServiceImpl implements WorkflowService {

    private final WorkflowUtil workflowUtil;
    private final WorkflowInfoMapper workflowInfoMapper;
    private final NodeDefaultsMapper nodeDefaultsMapper;
    private final NodeNextRelationMapper nodeNextRelationMapper;
    private final NodePreRelationMapper nodePreRelationMapper;
    private final NodeMapper nodeMapper;
    private final ConditionMapper conditionMapper;
    private final DataMapMapper dataMapMapper;
    private final RedisWorkflowServiceImpl redisWorkflowServiceImpl;

    public WorkflowServiceImpl(WorkflowInfoMapper workflowInfoMapper, NodeDefaultsMapper nodeDefaultsMapper, NodeNextRelationMapper nodeNextRelationMapper, NodePreRelationMapper nodePreRelationMapper, NodeMapper nodeMapper, ConditionMapper conditionMapper, DataMapMapper dataMapMapper, WorkflowUtil workflowUtil, RedisWorkflowServiceImpl redisWorkflowServiceImpl) {
        this.workflowInfoMapper = workflowInfoMapper;
        this.nodeDefaultsMapper = nodeDefaultsMapper;
        this.nodeNextRelationMapper = nodeNextRelationMapper;
        this.nodePreRelationMapper = nodePreRelationMapper;
        this.nodeMapper = nodeMapper;
        this.conditionMapper = conditionMapper;
        this.dataMapMapper = dataMapMapper;
        this.workflowUtil = workflowUtil;
        this.redisWorkflowServiceImpl = redisWorkflowServiceImpl;
    }

    /**
     * 添加工作流
     * @param workflowInfo 工作流信息
     * @return 添加结果
     */
    @Override
    @Transactional
    public int add(WorkflowInfo workflowInfo) {
        workflowInfo.setCreateTime(LocalDateTime.now());
        workflowInfo.setUpdateTime(LocalDateTime.now());

        int result = work(workflowInfo);
        if (result > 0) {
            // 从数据库重新获取完整的工作流信息
            WorkflowInfo savedWorkflow = workflowInfoMapper.getById(workflowInfo.getId());
            if (savedWorkflow != null) {
                // 添加到Redis
                redisWorkflowServiceImpl.addWorkflowToRedis(savedWorkflow);
                // 处理定时任务
                redisWorkflowServiceImpl.addScheduledTask(savedWorkflow);
            }
        }
        return result;
    }

    /**
     * 删除工作流
     * @param id 工作流ID
     * @return 删除结果
     */
    @Override
    @Transactional
    public int remove(String id) {
        int result = workflowInfoMapper.deleteById(id);
        if (result > 0) {
            // 从Redis中删除
            redisWorkflowServiceImpl.removeWorkflowFromRedis(id);
            // 移除定时任务
            redisWorkflowServiceImpl.removeScheduledTask(id);
        }
        return result;
    }

    @Override
    @Transactional
    public int edit(WorkflowInfo workflowInfo) {
        workflowInfo.setUpdateTime(LocalDateTime.now());
        
        // 先从Redis中删除旧数据
        redisWorkflowServiceImpl.removeWorkflowFromRedis(workflowInfo.getId());
        redisWorkflowServiceImpl.removeScheduledTask(workflowInfo.getId());
        
        workflowInfoMapper.deleteById(workflowInfo.getId());
        int result = work(workflowInfo);
        
        if (result > 0) {
            // 从数据库重新获取完整的工作流信息
            WorkflowInfo updatedWorkflow = workflowInfoMapper.getById(workflowInfo.getId());
            if (updatedWorkflow != null) {
                // 添加到Redis
                redisWorkflowServiceImpl.addWorkflowToRedis(updatedWorkflow);
                // 更新定时任务
                redisWorkflowServiceImpl.addScheduledTask(updatedWorkflow);
            }
        }
        return result;
    }

    /**
     * 修改工作流禁用原因
     * @param id 工作流ID
     * @param disableReason 禁用原因
     */
    @Override
    @Transactional
    public void editDisableReason(String id, String disableReason) {
        workflowInfoMapper.updateAvailableByWorkflowId(id, disableReason);
        redisWorkflowServiceImpl.removeScheduledTask(id);
        redisWorkflowServiceImpl.removeWorkflowFromRedis(id);
    }


    /**
     * 查询所有工作流
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 工作流列表
     */
    @Override
    public PageInfo<WorkflowInfo> findAll(String userId,int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<String> ids;
        if (userId != null){
            ids = workflowInfoMapper.selectIdsByUserId(userId);
        }else {
            ids = workflowInfoMapper.selectAllIds();
        }
        if (ids.isEmpty()) {
            return new PageInfo<>(new ArrayList<>());
        }

        List<WorkflowInfo> workflowInfos = workflowInfoMapper.selectAll(ids);

        PageInfo<WorkflowInfo> pageInfo = new PageInfo<>(workflowInfos);
        pageInfo.setTotal(workflowInfoMapper.countAll());

        return pageInfo;
    }

    /**
     * 查询工作流
     * @param id 工作流ID
     * @return 工作流信息
     */
    @Override
    public WorkflowInfo findById(String id) {
        return workflowInfoMapper.getById(id);
    }

    /**
     * 判断工作流是否存在
     * @param id 工作流ID
     * @return 是否存在
     */
    @Override
    public Boolean existsById(String id) {
        return workflowInfoMapper.existsById(id);
    }

    /**
     * 测试工作流
     * @param workflowInfo 工作流信息
     * @return 执行结果
     */
    @Override
    public JsonNode test(WorkflowInfo workflowInfo) throws Exception {
        return workflowUtil.executeWorkflow(workflowInfo,"",null);
    }

    private int work(WorkflowInfo workflowInfo) {
        List<Node> nodes = workflowInfo.getNodes();
        Map<String,String> dataMapId = new HashMap<>();
        for (Node node : nodes){
            String uuid = UUID.randomUUID().toString();
            dataMapId.put(node.getId(),uuid);
            node.setId(uuid);
        }
        nodes.forEach(node -> node.setWorkflowId(workflowInfo.getId()));

        List<NodeDefaults> nodeDefaults = nodes.stream()
                .flatMap(node -> node.getNodeDefaults()
                        .stream()
                        .peek(nodeDefault -> nodeDefault.setId(UUID.randomUUID().toString()))
                        .peek(nodeDefault -> nodeDefault.setNodeId(node.getId())))
                .toList();

        List<DataMap> dataMaps = nodes.stream()
                .flatMap(node -> node.getDataMaps()
                        .stream()
                        .peek(dataMap -> dataMap.setId(UUID.randomUUID().toString()))
                        .peek(dataMap -> dataMap.setNodeId(node.getId()))
                        .peek(dataMap -> dataMap.setSourceNodeId(dataMapId.get(dataMap.getSourceNodeId())))
                )
                .toList();

        List<Condition> conditions = nodes.stream()
                .filter(node -> node.getCondition() != null)
                .peek(node -> {
                    Condition condition = node.getCondition();
                    condition.setId(UUID.randomUUID().toString());
                    condition.setNodeId(node.getId());
                })
                .map(Node::getCondition)
                .toList();

        Map<String, List<String>> nodeNextRelation = nodes.stream()
                .collect(Collectors.toMap(
                        Node::getId,
                        node -> node.getNextNodeId()
                                .stream()
                                .map(dataMapId::get)
                                .toList()
                ))
                .entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<String, List<String>> nodePreRelation = nodes.stream()
                .collect(Collectors.toMap(
                        Node::getId,
                        node -> node.getPreNodeId()
                                .stream()
                                .map(dataMapId::get)
                                .toList()
                ))
                .entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        int insertWorkflowInfo = workflowInfoMapper.insert(workflowInfo);
        int insertNodes = nodeMapper.insert(nodes);
        int insertNodeDefaults = nodeDefaults.isEmpty() ? 1 : nodeDefaultsMapper.insert(nodeDefaults);
        int insertDataMaps = dataMaps.isEmpty() ? 1 : dataMapMapper.insert(dataMaps);
        int insertConditions = conditions.isEmpty() ? 1 : conditionMapper.insert(conditions);
        int insertNodeNextRelation = nodeNextRelation.isEmpty() ? 1 : nodeNextRelationMapper.insert(nodeNextRelation);
        int insertNodePreRelation = nodePreRelation.isEmpty() ? 1 : nodePreRelationMapper.insert(nodePreRelation);
        return (insertWorkflowInfo + insertNodes + insertNodeDefaults + insertDataMaps + insertConditions + insertNodeNextRelation + insertNodePreRelation) > 0 ? 1 : 0;
    }
}
