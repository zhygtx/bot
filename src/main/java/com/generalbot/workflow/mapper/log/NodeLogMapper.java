package com.generalbot.workflow.mapper.log;

import com.generalbot.workflow.entity.log.NodeLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 节点日志映射器
 */
@Mapper
public interface NodeLogMapper {

    /**
     * 添加节点日志（批量）
     */
    int insert(@Param("nodeLogs") List<NodeLog> nodeLogs);

    /**
     * 根据工作流日志ID查询节点日志
     */
    List<NodeLog> selectByWorkflowLogId(Long workflowLogId);
}