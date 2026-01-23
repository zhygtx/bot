package com.example.demo.service.task.actionContent.impl;

import com.example.demo.mapper.task.actionContent.ApiMapper;
import com.example.demo.mapper.task.actionContent.ApiParamsMapper;
import com.example.demo.pojo.task.actionContent.Api;
import com.example.demo.service.task.actionContent.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ApiServiceImpl implements ApiService {

    private final ApiMapper apiMapper;
    private final ApiParamsMapper apiParamsMapper;

    @Autowired
    public ApiServiceImpl(ApiMapper apiMapper, ApiParamsMapper apiParamsMapper) {
        this.apiMapper = apiMapper;
        this.apiParamsMapper = apiParamsMapper;
    }

    /**
     * 根据ID获取API调用内容
     * @param id API调用内容ID
     * @return API调用内容
     */
    @Override
    public Api getApiById(String id) {
        Api api = apiMapper.selectById(id);
        api.setParams(apiParamsMapper.select(id));
        return api;
    }

    /**
     * 根据用户ID获取API调用内容列表
     * @param userId 用户ID
     * @return API调用内容列表
     */
    @Override
    public List<Api> getApisByUserId(String userId) {
        List<Api> apis = apiMapper.getAllApis();
        for (Api api : apis) {
            api.setParams(apiParamsMapper.select(api.getId()));
        }
        return apis;
    }

    /**
     * 添加API调用内容
     * @param api API调用内容
     * @return API调用内容
     */
    @Override
    public Api addApi(Api api) {
        api.setId(UUID.randomUUID().toString());
        apiMapper.insert(api);
        if (api.getParams() != null){
            List<Map<String, String>> paramsList = new ArrayList<>();
            for (Map.Entry<String, String> entry : api.getParams().entrySet()){
                Map<String, String> params = Map.of(entry.getKey(), entry.getValue());
                paramsList.add(params);
            }
            apiParamsMapper.insert(paramsList, api.getId());
        }
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
        apiParamsMapper.delete(api.getId());
        if (api.getParams() != null){
            List<Map<String, String>> paramsList = new ArrayList<>();
            for (Map.Entry<String, String> entry : api.getParams().entrySet()){
                Map<String, String> params = Map.of(entry.getKey(), entry.getValue());
                paramsList.add(params);
            }
            apiParamsMapper.insert(paramsList, api.getId());
        }
        return api;
    }

    /**
     * 删除API调用内容
     * @param id API调用内容ID
     * @return 删除数量
     */
    @Override
    public int deleteApiById(String id) {
        int result = apiMapper.deleteById(id);
        apiParamsMapper.delete(id);
        return result;
    }
}
