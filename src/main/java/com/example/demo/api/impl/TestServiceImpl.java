package com.example.demo.api.impl;


import org.springframework.stereotype.Service;
import com.github.zhygtx.service.TestService;

@Service
public class TestServiceImpl implements TestService {

    @Override
    public String test(String s) {
        return "测试成功"+s;
    }
}
