package com.example.demo.mapper.plugin;

import com.example.demo.pojo.plugin.MethodInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 方法信息映射接口
 */
@Mapper
public interface MethodInfoMapper {

    /**
     * 批量插入方法信息
     * @param methodInfo 方法信息列表
     * @return 插入数量
     */
    int insert(List<MethodInfo> methodInfo);

    /**
     * 批量更新方法信息
     * @param methodInfo 方法信息列表
     * @return 更新数量
     */
    int update(List<MethodInfo> methodInfo);
}
