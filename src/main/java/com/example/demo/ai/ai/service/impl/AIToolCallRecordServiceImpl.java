package com.example.demo.ai.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.ai.ai.mapper.AIToolCallRecordMapper;
import com.example.demo.ai.ai.pojo.entity.AIToolCallRecord;
import com.example.demo.ai.ai.service.AIToolCallRecordService;
import org.springframework.stereotype.Service;

/**
 * 工具调用记录服务实现。
 */
@Service
public class AIToolCallRecordServiceImpl extends ServiceImpl<AIToolCallRecordMapper, AIToolCallRecord> implements AIToolCallRecordService {
}
