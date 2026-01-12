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
     * @param containerId
     */
    void deleteContainer(String containerId);
}
