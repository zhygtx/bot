package com.example.demo.service.task.actionContent.impl;

import com.example.demo.mapper.task.actionContent.ApiMapper;
import com.example.demo.pojo.task.actionContent.Api;
import com.example.demo.service.task.actionContent.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApiServiceImpl implements ApiService {

    private final ApiMapper apiMapper;

    @Autowired
    public ApiServiceImpl(ApiMapper apiMapper) {
        this.apiMapper = apiMapper;
    }

    /**
     * 获取API动作细节
     * @param id 动作ID
     * @return API动作细节
     */
    @Override
    public Api getApi(String id){
        return apiMapper.getApi(id);
    }
}
