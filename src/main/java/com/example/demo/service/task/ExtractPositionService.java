package com.example.demo.service.task;

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
}
