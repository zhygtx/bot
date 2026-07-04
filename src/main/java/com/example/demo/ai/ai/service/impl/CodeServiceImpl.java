package com.example.demo.ai.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.ai.ai.mapper.CodeMapper;
import com.example.demo.ai.ai.pojo.entity.Code;
import com.example.demo.ai.ai.service.CodeService;
import org.springframework.stereotype.Service;

@Service
public class CodeServiceImpl extends ServiceImpl<CodeMapper, Code> implements CodeService {
}
