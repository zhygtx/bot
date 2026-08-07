package com.generalbot.workflow.service;

import com.generalbot.workflow.dto.WorkflowInfoDto;
import com.generalbot.workflow.entity.WorkflowInfo;
import com.github.pagehelper.PageInfo;

/**
 * 工作流服务接口。
 */
public interface WorkflowService {

    /**
     * 新增工作流
     * @param workflowInfo 工作流信息
     * @return 保存后的工作流
     */
    WorkflowInfo add(WorkflowInfo workflowInfo);

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
     * 修改启用状态
     * @param id 工作流ID
     * @param enabled 是否启用
     * @return 修改结果
     */
    int editEnabled(String id, boolean enabled);

    /**
     * 禁用工作流并写入原因
     * @param id 工作流ID
     * @param disableReason 禁用原因
     */
    void disable(String id, String disableReason);

    /**
     * 禁用依赖某个插件的全部工作流
     * @param pluginId 插件ID
     * @param disableReason 禁用原因
     */
    void disableWorkflowsByPlugin(String pluginId, String disableReason);

    /**
     * 分页查询工作流
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    PageInfo<WorkflowInfoDto> findAll(String userId, int pageNum, int pageSize);

    /**
     * 根据ID查询工作流
     * @param id 工作流ID
     * @return 工作流信息
     */
    WorkflowInfo findById(String id);

    /**
     * 测试工作流
     * @param workflowId 工作流ID
     * @return 执行记录ID
     */
    Long test(String workflowId);
}
