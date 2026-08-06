package com.generalbot.docker.service.impl;

import com.generalbot.bot.mapper.BotMapper;
import com.generalbot.docker.mapper.DockerMapper;
import com.generalbot.bot.entity.BotInfo;
import com.generalbot.docker.entity.Docker;
import com.generalbot.common.api.Result;
import com.generalbot.docker.service.DockerService;
import com.generalbot.user.service.UserService;
import com.generalbot.docker.util.DockerUtil;
import com.generalbot.user.util.EmailUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class DockerServiceImpl implements DockerService {

    private final DockerUtil dockerUtil;
    private final DockerMapper dockerMapper;
    private final UserService userService;
    private final EmailUtil emailUtil;
    private final BotMapper botMapper;

    public DockerServiceImpl(DockerUtil dockerUtil, DockerMapper dockerMapper, UserService userService, EmailUtil emailUtil, BotMapper botMapper) {
        this.dockerUtil = dockerUtil;
        this.dockerMapper = dockerMapper;
        this.userService = userService;
        this.emailUtil = emailUtil;
        this.botMapper = botMapper;
    }

    /**
     * 创建容器并自动处理配置文件
     * @param userId 用户
     * @param token WEBUI_TOKEN
     * @param botQQ 机器人QQ
     */
    @Override
    public Result<?> createContainer(String userId, String token, Long botQQ){

        if (dockerMapper.isOverLimit()){
            return Result.error(400,"总容器数量超限");
        }

        // 查询 Bot 的鉴权 token 和路径后缀
        BotInfo botInfo = botMapper.selectByBotQQ(botQQ);
        if (botInfo == null) {
            return Result.error(400, "请先注册机器人再创建容器");
        }

        String containerName = "napcat_" + botQQ;
        log.info("开始创建容器，用户ID: {}, 容器名称: {}, token: {}", userId, containerName, token != null ? "***" : null);
        
        int hostPort;
        try {
            hostPort = dockerUtil.findAvailableHostPort();
            log.info("找到可用的主机端口: {}", hostPort);
        } catch (IOException e) {
            log.error("查找可用端口时发生错误: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
        
        String containerId = dockerUtil.createContainerWithAutoConfig(hostPort, containerName, token, botInfo.getToken(), botInfo.getPathSuffix(), botQQ);
        log.info("容器创建成功，容器ID: {}", containerId);

        Docker docker = new Docker();
        docker.setContainerId(containerId);
        docker.setName(containerName);
        docker.setUserId(userId);
        docker.setBotQQ(botQQ);
        docker.setPort(hostPort);
        docker.setToken(token);
        docker.setCreateTime(LocalDateTime.now());
        docker.setUpdateTime(LocalDateTime.now());
        dockerMapper.insert(docker);
        return Result.success(hostPort);
    }

    /**
     * 获取用户容器信息
     * @param userId 用户ID
     * @return 容器信息
     */
    @Override
    public Docker getByUserId(String userId){
        return dockerMapper.selectByUserId(userId);
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

    /**
     * 清理容器
     */
    @Override
    @Scheduled(initialDelay = 5 * 60 * 1000, fixedDelay = 5 * 60 * 1000)
    public void cleanDocker(){
        List<Docker> dockerList = dockerMapper.selectNeedUpdate();
        log.debug("需要更新容器列表: {}", dockerList);
        for (Docker docker : dockerList) {
            if (!dockerUtil.isContainerRunning(docker.getContainerId())){
                log.debug("容器 {} 已停止运行，删除容器记录", docker.getContainerId());
                dockerMapper.deleteByContainerId(docker.getContainerId());
            }
            List<String> files = dockerUtil.listFilesInContainer(docker.getContainerId());
            log.debug("容器 {} 中文件列表: {}", docker.getContainerId(), files);
            if (files.contains("napcat_"+ docker.getBotQQ() + ".json") || files.contains("onebot11_"+ docker.getBotQQ() + ".json")){
                log.debug("容器 {} 中登录的QQ与数据库中用户登记的QQ一致，更新容器信息", docker.getContainerId());
                docker.setUpdateTime(LocalDateTime.now());
                dockerMapper.update(docker);
            }else {
                log.debug("容器 {} 中登录的QQ与数据库中用户登记的QQ不一致，删除容器", docker.getContainerId());
                dockerUtil.deleteContainer(docker.getContainerId());
                dockerMapper.deleteByContainerId(docker.getContainerId());
                String email = userService.selectEmail(docker.getUserId());
                emailUtil.sendEmail(email, "容器删除通知", "由于您登录的QQ与登记的QQ并不一致现已被程序自动清除", false);

            }
        }
    }

}