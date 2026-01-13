package com.example.demo.service.task.actionContent;

import com.example.demo.pojo.task.actionContent.Api;

import java.util.List;

public interface ApiService {

    /**
     * 获取API动作细节
     * @param id 动作ID
     * @return API动作细节
     */
    Api getApi(String id);

    /**
     * 获取所有API调用内容
     * @return API调用内容列表
     */
    List<Api> getAllApis();

    /**
     * 根据ID获取API调用内容
     * @param id API调用内容ID
     * @return API调用内容
     */
    Api getApiById(String id);

    /**
     * 根据用户ID获取API调用内容列表
     * @param userId 用户ID
     * @return API调用内容列表
     */
    List<Api> getApisByUserId(String userId);

    /**
     * 添加API调用内容
     * @param api API调用内容
     * @return API调用内容
     */
    Api addApi(Api api);

    /**
     * 更新API调用内容
     * @param api API调用内容
     * @return API调用内容
     */
    Api updateApi(Api api);

    /**
     * 删除API调用内容
     * @param id API调用内容ID
     * @return 删除数量
     */
    int deleteApiById(String id);

}
