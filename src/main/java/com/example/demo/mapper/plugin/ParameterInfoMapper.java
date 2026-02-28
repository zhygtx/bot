package com.example.demo.mapper.plugin;

import com.example.demo.pojo.plugin.ParameterInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 参数信息映射接口
 */
@Mapper
public interface ParameterInfoMapper {

    /**
     * 批量插入参数信息
     * @param parameterInfo 参数信息列表
     * @return 插入结果
     */
    int insert(List<ParameterInfo> parameterInfo);

    /**
     * 批量更新参数信息
     * @param parameterInfo 参数信息列表
     * @return 更新结果
     */
    int update(List<ParameterInfo> parameterInfo);
}
