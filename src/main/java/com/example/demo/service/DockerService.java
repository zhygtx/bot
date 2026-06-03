package com.example.demo.service;

import com.example.demo.pojo.entity.Docker;
import com.example.demo.pojo.entity.Result;
import com.example.demo.pojo.entity.User;

/**
 * docker 服务接口
 */
public interface DockerService {

    /**
     * 创建容器
     * @param user 用户
     * @param token 容器令牌
     */
    Result<?> createContainer(User user, String token);

    /**
     * 获取docker信息
     * @param userId 用户id
     * @return docker信息
     */
    Docker getByUserId(String userId);

    /**
     * 删除容器
     * @param botQQ 机器人QQ
     */
    void deleteContainer(Long botQQ);

    /**
     * 删除容器
     * @param user 用户
     */
    void deleteContainer(User user);

    /**
     * 更新容器
     */
    @SuppressWarnings("unused")
    void cleanDocker();
}
