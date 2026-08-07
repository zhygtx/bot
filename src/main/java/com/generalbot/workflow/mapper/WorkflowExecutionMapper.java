package com.generalbot.workflow.mapper;

import com.generalbot.workflow.entity.execution.WorkflowExecution;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 工作流执行记录 Mapper。
 */
@Mapper
public interface WorkflowExecutionMapper {

    /**
     * 插入执行记录
     * @param execution 执行记录
     * @return 插入结果
     */
    int insert(WorkflowExecution execution);

    /**
     * 根据ID查询执行记录
     * @param id 执行记录ID
     * @return 执行记录
     */
    WorkflowExecution selectById(Long id);

    /**
     * 条件分页查询执行记录
     * @param userId 用户ID
     * @param workflowId 工作流ID
     * @param workflowName 工作流名称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param sortField 排序字段
     * @param sortOrder 排序方式
     * @param status 状态
     * @return 执行记录列表
     */
    List<WorkflowExecution> selectPage(@Param("userId") String userId,
                                       @Param("workflowId") String workflowId,
                                       @Param("workflowName") String workflowName,
                                       @Param("startTime") Long startTime,
                                       @Param("endTime") Long endTime,
                                       @Param("sortField") String sortField,
                                       @Param("sortOrder") String sortOrder,
                                       @Param("status") String status);

    /**
     * 按工作流ID聚合执行统计
     * @param ids 工作流ID列表
     * @return 统计结果
     */
    List<Map<String, Object>> selectStatsByWorkflowIds(@Param("ids") List<String> ids);
}
