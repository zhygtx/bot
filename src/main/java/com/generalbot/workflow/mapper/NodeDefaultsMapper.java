package com.generalbot.workflow.mapper;

import com.generalbot.workflow.entity.NodeDefaults;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 节点默认值Mapper
 */
@Mapper
public interface NodeDefaultsMapper {

    /**
     * 批量插入节点默认值
     * @param nodeDefaults 节点默认值列表
     * @return 插入数量
     */
    int insert(List<NodeDefaults> nodeDefaults);

}
