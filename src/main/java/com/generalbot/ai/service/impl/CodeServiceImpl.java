package com.generalbot.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.generalbot.ai.mapper.CodeMapper;
import com.generalbot.ai.entity.Code;
import com.generalbot.ai.service.CodeService;
import org.springframework.stereotype.Service;

@Service
public class CodeServiceImpl extends ServiceImpl<CodeMapper, Code> implements CodeService {
}
