package com.example.demo.mapper.plugin;

import com.example.demo.pojo.plugin.MethodClassInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 方法类信息映射接口
 */
@Mapper
public interface MethodClassInfoMapper {

    /**
     * 批量插入方法类信息
     * @param methodClassInfoList 方法类信息列表
     * @return 插入数量
     */
    int insert(List<MethodClassInfo> methodClassInfoList);

    /**
     * 批量更新方法类信息
     * @param methodClassInfoList 方法类信息列表
     * @return 更新数量
     */
    int update(List<MethodClassInfo> methodClassInfoList);
}
