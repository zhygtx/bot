package com.generalbot.docker.service;

import com.generalbot.docker.entity.Docker;
import com.generalbot.common.api.Result;

/**
 * docker 服务接口
 */
public interface DockerService {

    /**
     * 创建容器
     * @param userId 用户
     * @param token 容器令牌
     * @param botQQ botQQ号
     */
    Result<?> createContainer(String userId, String token, Long botQQ);

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
     * 更新容器
     */
    @SuppressWarnings("unused")
    void cleanDocker();
}
