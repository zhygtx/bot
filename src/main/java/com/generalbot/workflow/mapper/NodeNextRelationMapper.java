package com.generalbot.workflow.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface NodeNextRelationMapper {

    int insert(Map<String, List<String>> map);

    int deleteByNodeId(List<String> nodeId);

}
