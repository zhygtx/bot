package com.example.demo.handler;

import com.example.demo.docker.DockerService;
import com.example.demo.handler.utils.BotContext;
import com.example.demo.service.BotService;
import com.example.demo.service.EmailService;
import com.example.demo.service.UserService;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import com.mikuac.shiro.core.CoreEvent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义核心事件处理器，用于管理 Bot 的上线/下线以及在线状态缓存。
 * 此类继承自 Shiro SDK 的 CoreEvent，并被声明为 Spring 主组件 (@Primary)，
 * 以覆盖默认的核心事件处理逻辑。
 */
@Slf4j
@Primary
@Component
public class BotCoreEvent extends CoreEvent {

    // Bot 容器
    @Resource
    private BotContainer botContainer;

    private final EmailService emailService;
    private final BotService botService;
    private final BotContext botContext;
    private final DockerService dockerService;
    private final UserService userService;

    @Autowired
    public BotCoreEvent(BotService botService, BotContext botContext, EmailService emailService, DockerService dockerService, UserService userService) {
        this.botService = botService;
        this.botContext = botContext;
        this.emailService = emailService;
        this.dockerService = dockerService;
        this.userService = userService;
    }

    private final Map<Long, Map<Long, String>> botsCache = new ConcurrentHashMap<>();

    /**
     * 处理 Bot 上线事件。
     * 当 Bot 与服务端成功连接时调用此方法。
     * @param bot Bot 对象
     */
    @Override
    public void online(Bot bot) {
        // 1. 获取上线 Bot 的 QQ 号
        long botQQ = bot.getSelfId();

        // 2. 从 BotService 获取白名单
        Set<Long> whitelistedBots = botService.getAllBotQQs();

        // 3. 检查该 Bot 是否在白名单中
        if (whitelistedBots.contains(botQQ)) {
            // 4. 异步处理：延迟一段时间后再获取并缓存Bot信息
            CompletableFuture.runAsync(() -> {
                try {
                    // 延迟3秒等待Bot完全初始化
                    Thread.sleep(3000);

                    // 获取Bot群角色信息
                    Map<Long, String> botGroupRoles = botContext.getBotGroupRoles(bot);
                    botsCache.putIfAbsent(botQQ, botGroupRoles);

                    // 记录日志
                    log.info("[Bot 上线] QQ: {}, 已添加到在线缓存", botQQ);
                    botService.updateOnline(botQQ, true);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("[Bot 上线] QQ: {} 处理被中断", botQQ, e);
                } catch (Exception e) {
                    log.error("[Bot 上线] QQ: {} 信息获取失败", botQQ, e);
                }
            });

        } else {
            log.warn("[Bot 上线拒绝] QQ: {} 不在白名单中", botQQ);
        }
    }

    /**
     * 处理 Bot 下线事件。
     * 当 Bot 与服务端断开连接时调用此方法。
     * @param account 下线的 Bot 的 QQ 号
     */
    @Override
    public void offline(long account) {
        // 1. 从在线缓存中移除该 Bot 的信息
        Map<Long, String> removedBotData = botsCache.remove(account);

        // 2. 记录日志
        if (removedBotData != null) {
            String email = userService.selectEmail(account);
            emailService.sendEmail(email, "Bot下通知" , "您的QQBot已下线，如非手动下线请检查账号状态或联系管理员" , false);
            log.info("[Bot 下线] QQ: {}, 已从在线缓存移除", account);
            botService.updateOnline(account, false);
            dockerService.deleteContainer(account);
        } else {
            log.info("[Bot 下线] QQ: {}, 未在在线缓存中找到", account);
        }
    }

    /**
     * 处理 WebSocket 会话建立事件。
     * 在 Bot 尝试连接时调用，可用于连接前的身份验证。
     * @param session WebSocket 会话对象
     * @return true 允许连接，false 拒绝连接
     */
    @Override
    public boolean session(WebSocketSession session) {
        // 1. 从 WebSocket 握手头中尝试获取 Bot 的 QQ 号
        String botQQStr = session.getHandshakeHeaders().getFirst("x-self-id");

        // 2. 检查是否能获取到 QQ 号
        if (botQQStr != null) {
            try {
                // 3. 尝试解析 QQ 号
                long botQQ = Long.parseLong(botQQStr);

                // 4. 从 BotService 获取白名单
                Set<Long> whitelistedBots = botService.getAllBotQQs();

                // 5. 检查该 QQ 号是否在白名单中
                if (whitelistedBots.contains(botQQ)) {
                    // 6. 如果在白名单中，允许连接
                    log.debug("[WebSocket 连接] QQ: {} 在白名单中，允许连接", botQQ);
                    botService.updateOnline(botQQ, true);
                    return true;
                } else {
                    // 7. 如果不在白名单中，拒绝连接
                    log.warn("[WebSocket 连接拒绝] QQ: {} 不在白名单中", botQQ);
                    return false;
                }
            } catch (NumberFormatException e) {
                // 8. 如果 QQ 号格式无效，记录错误并拒绝连接
                log.error("[WebSocket 连接拒绝] 无效的 Bot QQ 号格式: {}", botQQStr, e);
                return false;
            }
        } else {
            // 9. 如果握手头中没有 QQ 号，记录警告并拒绝连接
            log.warn("[WebSocket 连接拒绝] 握手头中缺少 x-self-id");
            return false;
        }
    }

    /**
     * 定时更新在线缓存。
     */
    @Scheduled(fixedDelay = 60_000, initialDelay = 60_000)
    public void updateBotsCache() {
        log.debug("[Bot 缓存更新] 正在更新缓存...");
        if (botContainer.robots.isEmpty()){
            log.debug("[Bot 缓存更新] 无Bot在线");
            return;
        }

        Set<Long> botQQsSnapshot = new HashSet<>(botsCache.keySet());
        for (Long botQQ : botQQsSnapshot) {
            try {
                Bot bot = botContainer.robots.get(botQQ);
                Boolean online = bot.getStatus().getData().getOnline();
                if (online) {
                    log.debug("[Bot 缓存更新] QQ: {} 已更新", botQQ);
                    Map<Long, String> botGroupRoles = botContext.getBotGroupRoles(bot);
                    botsCache.put(botQQ, botGroupRoles);
                } else {
                    botsCache.remove(botQQ);
                    log.info("Bot[{}]意外离线", botQQ);
                    String email = userService.selectEmail(botQQ);
                    emailService.sendEmail(email, "Bot下通知" , "您的QQBot异常离线，如非手动下线请检查账号状态或联系管理员" , false);
                    log.info("[Bot 离线] QQ: {} 已从在线缓存移除", botQQ);
                    botService.updateOnline(botQQ, false);
                    dockerService.deleteContainer(botQQ);
                }
            } catch (Exception e) {
                log.error("更新Bot {} 缓存失败", botQQ, e);
            }
        }
    }




    /**
     * 获取所有当前已连接的 Bot 的 QQ 号。
     * @return 包含所有在线 Bot QQ 号的 Set 集合
     */
    public Set<Long> getBotQqs() {
        return new HashSet<>(botsCache.keySet());
    }

    /**
     * 获取在线 Bot 缓存的快照。
     * @return 在线 Bot 缓存的副本
     */
    public Map<Long, Map<Long, String>> getBotsCacheSnapshot() {
        return new HashMap<>(botsCache);
    }

    /**
     * 根据 Bot QQ 号获取其群角色映射。
     * @param botQQ Bot 的 QQ 号
     * @return Bot 的群角色映射副本，如果 Bot 不在线则返回 null
     */
    public Map<Long, String> getBotGroupRoles(Long botQQ) {
        Map<Long, String> roles = botsCache.get(botQQ);
        return roles != null ? new HashMap<>(roles) : null;
    }
}