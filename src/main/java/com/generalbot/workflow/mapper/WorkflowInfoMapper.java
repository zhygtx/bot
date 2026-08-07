package com.generalbot.workflow.mapper;

import com.generalbot.workflow.entity.WorkflowInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 工作流信息 Mapper。
 */
@Mapper
public interface WorkflowInfoMapper {

    /**
     * 插入工作流
     * @param workflowInfo 工作流信息
     * @return 插入结果
     */
    int insert(WorkflowInfo workflowInfo);

    /**
     * 更新工作流（名称、启用状态、触发键、定义）
     * @param workflowInfo 工作流信息
     * @return 更新结果
     */
    int update(WorkflowInfo workflowInfo);

    /**
     * 删除工作流
     * @param id 工作流ID
     * @return 删除结果
     */
    int deleteById(String id);

    /**
     * 更新启用状态
     * @param id 工作流ID
     * @param enabled 是否启用
     * @return 更新结果
     */
    @Update("UPDATE workflow SET enabled = #{enabled} WHERE id = #{id}")
    int updateEnabled(@Param("id") String id, @Param("enabled") boolean enabled);

    /**
     * 禁用工作流并写入原因
     *
     * @param id            工作流ID
     * @param disableReason 禁用原因
     */
    @Update("UPDATE workflow SET available = false, disable_reason = #{disableReason} WHERE id = #{id}")
    void disable(@Param("id") String id, @Param("disableReason") String disableReason);

    /**
     * 根据ID查询工作流
     * @param id 工作流ID
     * @return 工作流信息
     */
    WorkflowInfo selectById(String id);

    /**
     * 查询启用且可用的工作流（触发注册表初始化）
     * @return 工作流列表
     */
    List<WorkflowInfo> selectEnabled();

    /**
     * 查询全部工作流（用于插件删除时扫描依赖）
     * @return 工作流列表
     */
    List<WorkflowInfo> selectAll();

    /**
     * 查询用户的工作流ID列表
     * @param userId 用户ID
     * @return 工作流ID列表
     */
    List<String> selectIdsByUserId(String userId);

    /**
     * 查询全部工作流ID列表
     * @return 工作流ID列表
     */
    List<String> selectAllIds();

    /**
     * 计算工作流总数
     * @return 工作流总数
     */
    int countAll();
}
