package com.example.demo.mapper.log;

import com.example.demo.pojo.log.WorkflowLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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

}
