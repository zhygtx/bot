package com.example.demo.docker;

import com.example.demo.pojo.Result;

/**
 * docker 服务接口
 */
public interface DockerService {

    Result<String> createContainer();

}
