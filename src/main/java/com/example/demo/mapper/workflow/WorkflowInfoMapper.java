package com.example.demo.mapper.workflow;

import com.example.demo.pojo.workflow.WorkflowInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 工作流信息Mapper
 */
@Mapper
public interface WorkflowInfoMapper {

    /**
     * 插入工作流信息
     * @param workflowInfo 工作流信息
     * @return 插入结果
     */
    int insert(WorkflowInfo workflowInfo);

    /**
     * 根据ID删除工作流信息
     * @param id 工作流ID
     * @return 删除结果
     */
    int deleteById(String id);

    @Update("UPDATE workflow_info SET available = false, disable_reason = #{disableReason} " +
            "WHERE id IN (SELECT workflow_id FROM node WHERE plugin_id = #{pluginId})")
    void updateAvailable(String pluginId, String disableReason);

    @Update("UPDATE workflow_info SET available = false, disable_reason = #{disableReason} " +
            "WHERE id = #{id}")
    void updateAvailableByWorkflowId(String id, String disableReason);

    /**
     * 判断工作流是否存在
     * @param id 工作流ID
     * @return 是否存在
     */
    @Select("SELECT EXISTS(SELECT 1 FROM workflow_info WHERE id = #{id})")
    Boolean existsById(String id);

    /**
     * 获取所有工作流ID
     * @return 工作流ID列表
     */
    List<String> selectAllIds();

    @Select("SELECT id FROM workflow_info WHERE user_id = #{userId}")
    List<String> selectIdsByUserId(String userId);

    @Select("SELECT plugin_id FROM node WHERE plugin_id = #{pluginId}")
    List<String> selectIdsByPluginId(String pluginId);

    /**
     * 根据ID列表获取工作流信息
     * @param ids 工作流ID列表
     * @return 工作流信息列表
     */
    List<WorkflowInfo> selectAll(@Param("ids") List<String> ids);

    /**
     * 根据ID获取工作流信息
     * @param id 工作流ID
     * @return 工作流信息
     */
    WorkflowInfo getById(String id);

    /**
     * 计算工作流总数
     * @return 工作流总数
     */
    int countAll();

}
