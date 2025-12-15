package com.example.demo.service.task.actionContent;

import com.example.demo.pojo.task.actionContent.Api;

public interface ApiService {

    /**
     * 获取API动作细节
     * @param id 动作ID
     * @return API动作细节
     */
    Api getApi(String id);

}
