package com.example.demo.service.impl;

import com.example.demo.pojo.workflow.WorkflowInfo;
import com.example.demo.service.WorkflowEngineService;
import com.example.demo.util.ThreadLocalManager;
import com.example.demo.util.WorkflowUtil;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 工作流引擎服务实现类
 * 核心引擎，负责工作流的执行、节点调度、数据传递等
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowEngineServiceImpl implements WorkflowEngineService {

    private final WorkflowUtil workflowUtil;

    /**
     * 执行工作流的核心方法
     * @param workflowInfo 工作流信息
     * @return 执行结果
     */
    @Override
    public JsonNode executeWorkflow(WorkflowInfo workflowInfo) {
        log.info("开始执行工作流: {} (ID: {})", workflowInfo.getName(), workflowInfo.getId());

        try {
            // 1. 构建节点依赖关系图
            WorkflowUtil.WorkflowGraph graph = workflowUtil.buildWorkflowGraph(workflowInfo);

            // 2. 执行拓扑排序并逐个执行节点
            JsonNode result = workflowUtil.executeNodesInTopologicalOrder(graph, workflowInfo);

            log.info("工作流执行完成: {}", workflowInfo.getName());
            return result;

        } catch (Exception e) {
            log.error("工作流执行过程中发生错误: {}", e.getMessage(), e);
            return workflowUtil.executeNodesInTopologicalOrder(null, workflowInfo);
        } finally {
            // 清理线程本地变量
            ThreadLocalManager.clear();
        }
    }
}
