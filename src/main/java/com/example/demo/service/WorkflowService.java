package com.example.demo.service;

import com.example.demo.pojo.workflow.WorkflowInfo;
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
     * 查询所有工作流
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 工作流列表
     */
    PageInfo<WorkflowInfo> findAll(int pageNum, int pageSize);

}
