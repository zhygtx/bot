package com.generalbot.plugin.mapper;

import com.generalbot.plugin.entity.EntityInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 实体信息映射接口
 */
@Mapper
public interface EntityInfoMapper {

    /**
     * 批量插入实体信息
     * @param entityInfoList 实体信息列表
     * @return 插入数量
     */
    int insert(List<EntityInfo> entityInfoList);

    /**
     * 批量更新实体信息
     * @param entityInfoList 实体信息列表
     * @return 更新数量
     */
    int update(List<EntityInfo> entityInfoList);
}
