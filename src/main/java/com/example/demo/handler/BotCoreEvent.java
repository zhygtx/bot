package com.example.demo.handler;

import com.example.demo.service.BotService;
import com.example.demo.service.DockerService;
import com.example.demo.util.EmailUtil;
import com.github.zhygtx.napcat.auth.BotRegistrar;
import com.github.zhygtx.napcat.event.BotEventListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 自定义核心事件处理器，用于管理 Bot 的上线/下线以及在线状态缓存。
 * 此类继承自 Shiro SDK 的 CoreEvent，并被声明为 Spring 主组件 (@Primary)，
 * 以覆盖默认的核心事件处理逻辑。
 */
@Slf4j
@Primary
@Component
public class BotCoreEvent  implements BotEventListener {

    private final BotRegistrar botRegistrar;
    private final BotService botService;
    private final DockerService dockerService;
    private final EmailUtil emailUtil;

    public BotCoreEvent(BotService botService, DockerService dockerService, EmailUtil emailUtil, BotRegistrar botRegistrar) {
        this.botService = botService;
        this.dockerService = dockerService;
        this.emailUtil = emailUtil;
        this.botRegistrar = botRegistrar;
    }

    /**
     * 处理 Bot 上线事件。
     * 当 Bot 与服务端成功连接时调用此方法。
     * @param botQQ Bot 对象
     */
    @Override
    public void botOnline(Long botQQ) {
        botService.updateOnline(botQQ, true);
    }

    /**
     * 处理 Bot 下线事件。
     * 当 Bot 与服务端断开连接时调用此方法。
     * @param botQQ 下线的 Bot 的 QQ 号
     */
    @Override
    public void botOffline(Long botQQ) {
        String email = botService.selectEmail(botQQ);
        if (email != null) {
            emailUtil.sendEmail(email, "Bot下线通知" , "您的QQBot已下线，如非手动下线请检查账号状态或联系管理员" , false);
        }
        log.info("[Bot 下线] QQ: {}, 已从在线缓存移除", botQQ);
        botService.updateOnline(botQQ, false);
        dockerService.deleteContainer(botQQ);
    }
    
    /**
     * 应用准备就绪事件处理方法。
     * 当应用启动完成后调用此方法，注册所有 Bot。
     */
    @EventListener(ApplicationReadyEvent.class)
    public void task(){
        List<Map<String, String>> mapList = botService.selectPathSuffix();
        if (mapList.isEmpty()){
            return;
        }
        Map<String, String> map = mapList.get(0);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String pathSuffix = entry.getKey();
            String token = entry.getValue();
            botRegistrar.register(pathSuffix, token);
        }
    }
}