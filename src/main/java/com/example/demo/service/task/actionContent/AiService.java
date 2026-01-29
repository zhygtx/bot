package com.example.demo.service.task.actionContent;

import com.example.demo.pojo.task.actionContent.Ai;

import java.util.List;

public interface AiService {
    /**
     * 根据ID获取AI配置
     * @param id AI配置ID
     * @return AI配置
     */
    Ai getAiById(String id);

    /**
     * 根据用户ID获取AI配置列表
     * @param userId 用户ID
     * @return AI配置列表
     */
    List<Ai> getAisByUserId(String userId);

    /**
     * 添加AI配置
     * @param ai AI配置
     * @return AI配置
     */
    Ai addAi(Ai ai);

    /**
     * 更新AI配置
     * @param ai AI配置
     * @return AI配置
     */
    Ai updateAi(Ai ai);

    /**
     * 删除AI配置
     * @param id AI配置ID
     * @return 删除数量
     */
    int deleteAiById(String id);

}