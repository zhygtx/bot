package com.example.demo.docker.impl;

import com.example.demo.docker.DockerService;
import com.example.demo.mapper.DockerMapper;
import com.example.demo.pojo.Docker;
import com.example.demo.pojo.User;
import com.example.demo.service.EmailService;
import com.example.demo.service.UserService;
import com.example.demo.utils.DockerUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings("LoggingSimilarMessage")
@Service
@Slf4j
public class DockerServiceImpl implements DockerService {

    private final EmailService emailService;
    private final DockerUtil dockerUtil;
    private final DockerMapper dockerMapper;
    private final UserService userService;

    @Autowired
    public DockerServiceImpl(DockerUtil dockerUtil, DockerMapper dockerMapper, EmailService emailService, UserService userService) {
        this.dockerUtil = dockerUtil;
        this.dockerMapper = dockerMapper;
        this.emailService = emailService;
        this.userService = userService;
    }

    /**
     * 创建容器并自动处理配置文件
     * @param user 用户
     * @param token token
     */
    @Override
    public Integer createContainer(User user,String token){
        String containerName = "napcat_" + user.getQQ();
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
        docker.setPort(hostPort);
        docker.setToken(token);
        docker.setCreateTime(LocalDateTime.now());
        docker.setUpdateTime(LocalDateTime.now());
        dockerMapper.insert(docker);
        return hostPort;
    }

    /**
     * 删除容器
     * @param containerId 容器ID
     */
    @Override
    public void deleteContainer(String containerId){
        dockerUtil.deleteContainer(containerId);
        dockerMapper.deleteByContainerId(containerId);
    }

    /**
     * 删除容器
     * @param botQQ botQQ
     */
    @Override
    public void deleteContainer(Long botQQ){
        Docker docker = dockerMapper.selectByBotQQ(botQQ);
        if (docker == null){
            return;
        }
        String containerId = docker.getContainerId();
        dockerUtil.deleteContainer(containerId);
        dockerMapper.deleteByContainerId(containerId);
    }

    @Override
    @Scheduled(initialDelay = 5 * 60 * 1000, fixedDelay = 5 * 60 * 1000)
    public void cleanDocker(){
        List<Docker> dockerList = dockerMapper.selectNeedUpdate();
        for (Docker docker : dockerList) {
            List<String> files = dockerUtil.listFilesInContainer(docker.getContainerId());
            if (!files.contains("napcat_"+ docker.getBotQQ())){
                log.debug("容器 {} 中登录的QQ与数据库中用户登记的QQ不一致，删除容器", docker.getContainerId());
                dockerUtil.deleteContainer(docker.getContainerId());
                dockerMapper.deleteByContainerId(docker.getContainerId());
                String email = userService.selectEmail(docker.getUserId());
                emailService.sendEmail(email, "容器删除通知", "由于您登录的QQ与登记的QQ并不一致现已被程序自动清除", false);
            }else {
                log.debug("容器 {} 中登录的QQ与数据库中用户登记的QQ一致，更新容器信息", docker.getContainerId());
                docker.setUpdateTime(LocalDateTime.now());
                dockerMapper.update(docker);
            }
        }
    }

}