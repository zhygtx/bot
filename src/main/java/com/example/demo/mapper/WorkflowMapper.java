package com.example.demo.mapper;

import com.example.demo.pojo.workflow.WorkflowInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 工作流信息映射接口
 */
@Mapper
public interface WorkflowMapper {

    /**
     * 插入工作流信息
     * @param workflowInfo 工作流信息
     */
    int insertWorkflow(WorkflowInfo workflowInfo);

    /**
     * 根据ID查询工作流信息
     * @param id 工作流ID
     * @return 工作流信息
     */
    WorkflowInfo selectWorkflowById(String id);

    /**
     * 查询所有工作流信息
     * @return 工作流信息列表
     */
    List<WorkflowInfo> selectAllWorkflows();

    /**
     * 更新工作流信息
     * @param workflowInfo 工作流信息
     */
    int updateWorkflow(WorkflowInfo workflowInfo);

    /**
     * 删除工作流信息
     * @param id 工作流ID
     */
    int deleteWorkflow(String id);

    /**
     * 根据用户ID查询工作流信息
     * @param userId 用户ID
     * @return 工作流信息列表
     */
    List<WorkflowInfo> selectWorkflowsByUserId(String userId);
}
