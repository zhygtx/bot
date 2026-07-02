package com.example.demo.ai.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.ai.ai.mapper.UserAIConfigMapper;
import com.example.demo.ai.ai.pojo.entity.UserAIConfig;
import com.example.demo.ai.ai.service.UserAIConfigService;
import org.springframework.stereotype.Service;

@Service
public class UserAIConfigServiceImpl extends ServiceImpl<UserAIConfigMapper, UserAIConfig> implements UserAIConfigService {
}
