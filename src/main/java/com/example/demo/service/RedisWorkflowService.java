package com.example.demo.service;

import com.example.demo.pojo.workflow.WorkflowInfo;

import java.util.List;

public interface RedisWorkflowService {

    void addWorkflowToRedis(WorkflowInfo workflowInfo);

    void removeWorkflowFromRedis(String workflowId);

    List<WorkflowInfo> getWorkflowsByBotEvent(Long botQQ, String eventType);

    void addScheduledTask(WorkflowInfo workflowInfo);

    void removeScheduledTask(String workflowId);

    List<WorkflowInfo> getScheduledTasks();

}
