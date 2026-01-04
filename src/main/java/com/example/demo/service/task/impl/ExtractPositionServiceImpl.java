package com.example.demo.service.task.impl;

import com.example.demo.mapper.task.ExtractPositionMapper;
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

}
