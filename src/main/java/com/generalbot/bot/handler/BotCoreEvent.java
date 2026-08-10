package com.generalbot.bot.handler;

import com.generalbot.bot.service.BotService;
import com.generalbot.docker.service.DockerService;
import com.generalbot.user.util.EmailUtil;
import com.github.zhygtx.napcat.auth.BotRegistrar;
import com.github.zhygtx.napcat.event.BotEventListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ContextClosedEvent;
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

    /**
     * 服务器是否正在关闭。
     * volatile 保证多线程可见性。
     */
    private volatile boolean serverShuttingDown = false;

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
        // 记录上线状态和本次上线时间戳，统计首页据此计算“本次已在线时长”
        botService.updateOnline(botQQ, true, System.currentTimeMillis());
    }

    /**
     * 处理 Bot 下线事件。
     * 当 Bot 与服务端断开连接时调用此方法。
     * @param botQQ 下线的 Bot 的 QQ 号
     */
    @Override
    public void botOffline(Long botQQ) {
        log.info("[Bot 下线] QQ: {}, 已从在线缓存移除", botQQ);
        // 离线时清空上线时间戳，避免展示上一次连接的过期时长
        botService.updateOnline(botQQ, false, null);
        dockerService.deleteContainer(botQQ);

        if (serverShuttingDown) {
            log.info("[Bot 下线] 服务器正在关闭，跳过邮件通知: QQ={}", botQQ);
            return;
        }

        String email = botService.selectEmail(botQQ);
        if (email != null) {
            emailUtil.sendEmail(email, "Bot下线通知",
                    "您的QQBot已下线，如非手动下线请检查账号状态或联系管理员", false);
        }
    }

    /**
     * 容器关闭前设置标志位，避免 botOffline 中误发邮件。
     */
    @EventListener(ContextClosedEvent.class)
    public void onServerShutdown() {
        serverShuttingDown = true;
        log.info("[服务器关闭] 已设置关闭标志，Bot 下线时将跳过邮件通知");
    }
    
    /**
     * 应用准备就绪事件处理方法。
     * 当应用启动完成后调用此方法，注册所有 Bot。
     */
    @EventListener(ApplicationReadyEvent.class)
    public void task(){
        List<Map<String, String>> mapList = botService.selectPathSuffix();
        log.debug("从数据库加载到 {} 个 Bot 注册信息", mapList.size());
        if (mapList.isEmpty()){
            return;
        }
        for (Map<String, String> map : mapList) {
            String pathSuffix = map.get("path_suffix");
            String token = map.get("token");
            if (pathSuffix != null) {
                botRegistrar.register(pathSuffix, token);
                log.debug("注册 Bot: pathSuffix={}, token={}", pathSuffix, token != null ? "***" : "null");
            }
        }
    }
}
