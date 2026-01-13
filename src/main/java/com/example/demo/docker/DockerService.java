package com.example.demo.docker;

import com.example.demo.pojo.User;

/**
 * docker 服务接口
 */
public interface DockerService {

    /**
     * 创建容器
     * @param user 用户
     * @param token 容器令牌
     */
    Integer createContainer(User user, String token);

    /**
     * 删除容器
     * @param containerId 容器ID
     */
    void deleteContainer(String containerId);

    /**
     * 删除容器
     * @param botQQ 机器人QQ
     */
    void deleteContainer(Long botQQ);

    /**
     * 更新容器
     */
    @SuppressWarnings("unused")
    void cleanDocker();
}
