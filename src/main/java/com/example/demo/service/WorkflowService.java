package com.example.demo.service;

import com.example.demo.pojo.dto.WorkflowInfoDto;
import com.example.demo.pojo.entity.workflow.WorkflowInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.pagehelper.PageInfo;

/**
 * 工作流服务接口
 */
public interface WorkflowService {

    /**
     * 添加工作流
     * @param workflowInfo 工作流信息
     * @return 添加结果
     */
    int add(WorkflowInfo workflowInfo);

    /**
     * 删除工作流
     * @param id 工作流ID
     * @return 删除结果
     */
    int remove(String id);

    /**
     * 修改工作流
     * @param workflowInfo 工作流信息
     * @return 修改结果
     */
    int edit(WorkflowInfo workflowInfo);

    /**
     * 修改工作流启用状态
     * @param id 工作流ID
     * @param enabled 启用状态
     * @return 修改结果
     */
    int editEnabled(String id, boolean enabled);

    /**
     * 修改工作流禁用原因
     * @param id 工作流ID
     * @param disableReason 禁用原因
     */
    void editDisableReason(String id, String disableReason);

    /**
     * 查询所有工作流
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 工作流列表
     */
    PageInfo<WorkflowInfoDto> findAll(String userId, int pageNum, int pageSize);

    /**
     * 根据ID查询工作流
     * @param id 工作流ID
     * @return 工作流信息
     */
    WorkflowInfo findById(String id);

    /**
     * 判断工作流是否存在
     * @param id 工作流ID
     * @return 是否存在
     */
    Boolean existsById(String id);

    /**
     * 测试工作流
     * @param workflowInfo 工作流信息
     * @return 测试结果
     */
    JsonNode test(WorkflowInfo workflowInfo) throws Exception;
}
