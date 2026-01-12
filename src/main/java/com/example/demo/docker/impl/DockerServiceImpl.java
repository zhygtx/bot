package com.example.demo.docker.impl;

import com.example.demo.docker.DockerService;
import com.example.demo.mapper.DockerMapper;
import com.example.demo.pojo.Docker;
import com.example.demo.pojo.User;
import com.example.demo.utils.DockerUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;

@SuppressWarnings("LoggingSimilarMessage")
@Service
@Slf4j
public class DockerServiceImpl implements DockerService {

    private final DockerUtil dockerUtil;
    private final DockerMapper dockerMapper;

    @Autowired
    public DockerServiceImpl(DockerUtil dockerUtil, DockerMapper dockerMapper) {
        this.dockerUtil = dockerUtil;
        this.dockerMapper = dockerMapper;
    }

    @Override
    @Scheduled(initialDelay = 15000, fixedDelay = Long.MAX_VALUE)
    public void createContainer(){
        User user = new User();
        user.setName("test");
        String containerName = "test";
        String token = "gbx2004817";
        log.info("开始创建容器，用户: {}, 容器名称: {}, token: {}", user.getName(), containerName, token != null ? "***" : null);
        
        int hostPort;
        try {
            hostPort = dockerUtil.findAvailableHostPort();
            log.info("找到可用的主机端口: {}", hostPort);
        } catch (IOException e) {
            log.error("查找可用端口时发生错误: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
        
        String containerId = dockerUtil.createContainerWithAutoConfig(hostPort, containerName, token);
        log.info("容器创建成功，容器ID: {}", containerId);

        Docker docker = new Docker();
        docker.setContainerId(containerId);
        docker.setName(containerName);
        docker.setUserId(user.getId());
        docker.setBotQQ(user.getQQ());
        docker.setPort(hostPort + "");
        docker.setToken(token);
        docker.setCreateTime(LocalDateTime.now());
        docker.setUpdateTime(LocalDateTime.now());
        dockerMapper.insert(docker);
    }

    /**
     * 创建容器并自动处理配置文件
     * @param containerId 容器ID
     */
    @Override
    public void deleteContainer(String containerId){
        dockerUtil.deleteContainer(containerId);
        dockerMapper.deleteByContainerId(containerId);
    }

}