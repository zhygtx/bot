package com.example.demo.service.task.impl;

import com.example.demo.mapper.task.ExtractPositionMapper;
import com.example.demo.pojo.task.ExtractPosition;
import com.example.demo.service.task.ExtractPositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 提取位置服务实现类
 */
@Service
public class ExtractPositionServiceImpl implements ExtractPositionService {

    private final ExtractPositionMapper extractPositionMapper;

    @Autowired
    public ExtractPositionServiceImpl(ExtractPositionMapper extractPositionMapper) {
        this.extractPositionMapper = extractPositionMapper;
    }

    /**
     * 获取作用域的提取位置
     * @param roleId 作用域ID
     * @return 提取位置列表
     */
    @Override
    public List<Integer> getExtractPosition(String roleId) {
        return extractPositionMapper.getExtractPosition(roleId);
    }

    /**
     * 添加提取位置
     * @param extractPosition 提取位置
     * @return 提取位置
     */
    @Override
    public ExtractPosition addExtractPosition(ExtractPosition extractPosition) {
        extractPositionMapper.insert(extractPosition);
        return extractPosition;
    }

    /**
     * 更新提取位置
     * @param extractPosition 提取位置
     * @return 提取位置
     */
    @Override
    public ExtractPosition updateExtractPosition(ExtractPosition extractPosition) {
        extractPositionMapper.update(extractPosition);
        return extractPosition;
    }

    /**
     * 删除提取位置
     * @param id 提取位置ID
     * @return 删除数量
     */
    @Override
    public int deleteExtractPosition(Long id) {
        return extractPositionMapper.deleteById(id);
    }

    /**
     * 根据Role ID删除提取位置
     * @param roleId Role ID
     * @return 删除数量
     */
    @Override
    public int deleteExtractPositionsByRoleId(String roleId) {
        return extractPositionMapper.deleteByRoleId(roleId);
    }

    /**
     * 根据ID获取提取位置
     * @param id 提取位置ID
     * @return 提取位置
     */
    @Override
    public ExtractPosition getExtractPositionById(Long id) {
        return extractPositionMapper.selectById(id);
    }

    /**
     * 根据Role ID获取提取位置列表
     * @param roleId Role ID
     * @return 提取位置列表
     */
    @Override
    public List<ExtractPosition> getExtractPositionsByRoleId(String roleId) {
        return extractPositionMapper.selectByRoleId(roleId);
    }

}
