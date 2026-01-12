package com.example.demo.docker;

/**
 * docker 服务接口
 */
public interface DockerService {

    /**
     * 创建容器
     */
    void createContainer();

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
}
