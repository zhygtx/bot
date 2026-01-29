package com.example.demo.service.task.actionContent.impl;

import com.example.demo.mapper.task.actionContent.AiMapper;
import com.example.demo.pojo.task.actionContent.Ai;
import com.example.demo.service.task.actionContent.AiService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AiServiceImpl implements AiService {

    private final AiMapper aiMapper;

    public AiServiceImpl(AiMapper aiMapper) {
        this.aiMapper = aiMapper;
    }

    /**
     * 根据ID获取AI配置
     * @param id AI配置ID
     * @return AI配置
     */
    @Override
    public Ai getAiById(String id) {
        return aiMapper.selectById(id);
    }

    /**
     * 根据用户ID获取AI配置列表
     * @param userId 用户ID
     * @return AI配置列表
     */
    @Override
    public List<Ai> getAisByUserId(String userId) {
        return aiMapper.selectByUserId(userId);
    }

    /**
     * 添加AI配置
     * @param ai AI配置
     * @return AI配置
     */
    @Override
    @Transactional
    public Ai addAi(Ai ai) {
        ai.setId(UUID.randomUUID().toString());
        aiMapper.insert(ai);
        return ai;
    }

    /**
     * 更新AI配置
     * @param ai AI配置
     * @return AI配置
     */
    @Override
    @Transactional
    public Ai updateAi(Ai ai) {
        aiMapper.update(ai);
        return ai;
    }

    /**
     * 删除AI配置
     * @param id AI配置ID
     * @return 删除数量
     */
    @Override
    @Transactional
    public int deleteAiById(String id) {
        return aiMapper.deleteById(id);
    }

}