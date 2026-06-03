package com.example.demo.mapper.log;

import com.example.demo.pojo.log.NodeLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 节点日志映射器
 */
@Mapper
public interface NodeLogMapper {

    /**
     * 添加节点日志
     * @param nodeLogs 节点日志列表
     * @return 添加结果
     */
    int insert(@Param("nodeLogs") List<NodeLog> nodeLogs);

    /**
     * 根据工作流日志ID查询节点日志
     * @param workflowLogId 工作流日志ID
     * @return 节点日志列表
     */
    List<NodeLog> selectByWorkflowLogId(String workflowLogId);
}
