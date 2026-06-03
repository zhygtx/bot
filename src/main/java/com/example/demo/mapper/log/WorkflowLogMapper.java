package com.example.demo.mapper.log;

import com.example.demo.pojo.entity.log.WorkflowLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface WorkflowLogMapper {

    /**
     * 添加工作流日志
     * @param workflowLog 工作流日志
     * @return 添加结果
     */
    @Insert("insert into workflow_log(id, workflow_id, user_id, expected_node_count, actual_node_count, execution_time, start_time, initial_context, workflow_name) " +
            "values(#{id}, #{workflowId}, #{userId}, #{expectedNodeCount}, #{actualNodeCount}, #{executionTime}, #{startTime}, #{initialContext}, #{workflowName})")
    int insert(WorkflowLog workflowLog);

    /**
     * 根据用户ID查询工作流日志
     * @param userId 用户ID
     * @return 工作流日志列表
     */
    @Select("select * from workflow_log where user_id = #{userId}")
    List<WorkflowLog> selectByUserId(String userId);

    /**
     * 根据条件查询工作流日志
     * @param userId 用户ID
     * @param workflowName 工作流名称
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 工作流日志列表
     */
    List<WorkflowLog> selectByCondition(@Param("userId") String userId,
                                        @Param("workflowName") String workflowName,
                                        @Param("startTime") Long startTime,
                                        @Param("endTime") Long endTime);

    /**
     * 根据工作流ID列表批量获取统计信息
     * @param workflowIds 工作流ID列表
     * @return 统计信息列表，key为workflowId，value为统计信息
     */
    List<Map<String, Object>> selectStatsByWorkflowIds(@Param("workflowIds") List<String> workflowIds);

}
