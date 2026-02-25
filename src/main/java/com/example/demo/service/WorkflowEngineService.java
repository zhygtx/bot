package com.example.demo.service;

import com.example.demo.pojo.workflow.WorkflowInfo;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * 工作流引擎服务接口
 * 提供工作流执行的核心功能
 */
public interface WorkflowEngineService {

    /**
     * 执行工作流
     * @param workflowInfo 工作流信息
     * @return 执行结果
     */
    JsonNode executeWorkflow(WorkflowInfo workflowInfo);
}