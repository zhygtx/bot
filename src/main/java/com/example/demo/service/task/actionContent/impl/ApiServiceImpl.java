package com.example.demo.service.task.actionContent.impl;

import com.example.demo.mapper.task.actionContent.ApiMapper;
import com.example.demo.pojo.task.actionContent.Api;
import com.example.demo.service.task.actionContent.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    /**
     * 获取所有API调用内容
     * @return API调用内容列表
     */
    @Override
    public List<Api> getAllApis() {
        return apiMapper.getAllApis();
    }

    /**
     * 根据ID获取API调用内容
     * @param id API调用内容ID
     * @return API调用内容
     */
    @Override
    public Api getApiById(String id) {
        return apiMapper.selectById(id);
    }

    /**
     * 根据用户ID获取API调用内容列表
     * @param userId 用户ID
     * @return API调用内容列表
     */
    @Override
    public List<Api> getApisByUserId(String userId) {
        return apiMapper.selectByUserId(userId);
    }

    /**
     * 添加API调用内容
     * @param api API调用内容
     * @return API调用内容
     */
    @Override
    public Api addApi(Api api) {
        apiMapper.insert(api);
        return api;
    }

    /**
     * 更新API调用内容
     * @param api API调用内容
     * @return API调用内容
     */
    @Override
    public Api updateApi(Api api) {
        apiMapper.update(api);
        return api;
    }

    /**
     * 删除API调用内容
     * @param id API调用内容ID
     * @return 删除数量
     */
    @Override
    public int deleteApiById(String id) {
        return apiMapper.deleteById(id);
    }
}
