package com.generalbot.workflow.mapper.log;

import com.generalbot.workflow.entity.log.WorkflowLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface WorkflowLogMapper {

    /**
     * 添加工作流日志（主键自增）
     */
    @Insert("insert into workflow_log(workflow_id, user_id, expected_node_count, actual_node_count, execution_time, start_time, initial_context, workflow_name, error_log, is_error) " +
            "values(#{workflowId}, #{userId}, #{expectedNodeCount}, #{actualNodeCount}, #{executionTime}, #{startTime}, #{initialContext}, #{workflowName}, #{errorLog}, #{isError})")
    int insert(WorkflowLog workflowLog);

    /**
     * 根据条件查询工作流日志
     */
    List<WorkflowLog> selectByCondition(@Param("userId") String userId,
                                        @Param("workflowId") String workflowId,
                                        @Param("workflowName") String workflowName,
                                        @Param("startTime") Long startTime,
                                        @Param("endTime") Long endTime,
                                        @Param("sortField") String sortField,
                                        @Param("sortOrder") String sortOrder,
                                        @Param("status") String status);

    /**
     * 根据工作流ID列表批量获取统计信息
     */
    List<Map<String, Object>> selectStatsByWorkflowIds(@Param("workflowIds") List<String> workflowIds);

    /**
     * 根据ID查询工作流日志
     */
    WorkflowLog selectById(Long id);

    /**
     * 获取插入后的自增ID
     */
    Long getLastInsertId();
}