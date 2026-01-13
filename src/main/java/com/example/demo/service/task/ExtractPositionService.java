package com.example.demo.service.task;

import com.example.demo.pojo.task.ExtractPosition;

import java.util.List;

/**
 * 提取位置服务接口
 */
public interface ExtractPositionService {

    /**
     * 获取作用域的提取位置
     * @param roleId 规则ID
     * @return 提取位置列表
     */
    List<Integer> getExtractPosition(String roleId);

    /**
     * 添加提取位置
     * @param extractPosition 提取位置
     * @return 提取位置
     */
    ExtractPosition addExtractPosition(ExtractPosition extractPosition);

    /**
     * 更新提取位置
     * @param extractPosition 提取位置
     * @return 提取位置
     */
    ExtractPosition updateExtractPosition(ExtractPosition extractPosition);

    /**
     * 删除提取位置
     * @param id 提取位置ID
     * @return 删除数量
     */
    int deleteExtractPosition(Long id);

    /**
     * 根据Role ID删除提取位置
     * @param roleId Role ID
     * @return 删除数量
     */
    int deleteExtractPositionsByRoleId(String roleId);

    /**
     * 根据ID获取提取位置
     * @param id 提取位置ID
     * @return 提取位置
     */
    ExtractPosition getExtractPositionById(Long id);

    /**
     * 根据Role ID获取提取位置列表
     * @param roleId Role ID
     * @return 提取位置列表
     */
    List<ExtractPosition> getExtractPositionsByRoleId(String roleId);

}
