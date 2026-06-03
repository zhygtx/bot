package com.example.demo.mapper.workflow;

import com.example.demo.pojo.entity.workflow.Node;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 节点Mapper
 */
@Mapper
public interface NodeMapper {

    /**
     * 批量插入节点
     * @param nodes 节点列表
     * @return 插入数量
     */
    int insert(List<Node> nodes);

    /**
     * 根据工作流ID批量删除节点
     * @param workflowId 工作流ID
     * @return 删除数量
     */
    int deleteByWorkflowId(String workflowId);
}
