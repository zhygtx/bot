package com.news.bot.wf.task;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import com.news.bot.config.BotConfig;
import com.news.bot.task.BaseScheduledTask;
import com.news.bot.wf.pojo.Fissure;
import com.news.bot.wf.pojo.FissureTask;
import com.news.bot.wf.service.FissureService;
import com.news.bot.wf.utils.FissureTaskUtil;
import com.news.bot.wf.utils.FissuresUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FissureUpdateTask extends BaseScheduledTask{

    public FissureUpdateTask() {
        super("裂隙更新", "裂隙更新", Duration.ofMinutes(1));
    }

    @Override
    public void task() {
        updateFissureData();
    }

    @Autowired
    private FissuresUtil fissuresUtil;

    @Resource
    private BotContainer botContainer;

    // 使用新的接口替代直接依赖
    @Autowired
    private FissureService fissureService;

    @Autowired
    private BotConfig botConfig;

    public void updateFissureData() {
        long botId = botConfig.getBotId();
        Bot bot = botContainer.robots.get(botId);

        log.debug("--------------------------------------------------");
        log.debug("开始更新裂隙数据...");

        fissuresUtil.getAndConvertFissuresAsync()
                .thenApply(json -> {
                    log.debug("获取到原始JSON数据，长度: {}", json.length());
                    try {
                        List<Fissure> fissures = fissuresUtil.parseFissures(json);
                        log.info("成功解析裂隙数据，共 {} 条", fissures.size());
                        return fissures;
                    } catch (IOException e) {
                        log.error("解析裂隙数据时发生错误", e);
                        throw new RuntimeException(e);
                    }
                })
                .thenAccept(fissures -> {
                    try {
                        log.debug("进入数据处理阶段");

                        if (fissures == null || fissures.isEmpty()) {
                            log.warn("未获取到任何裂隙数据");
                            return;
                        }

                        try{
                            fissuresUtil.setEta(fissures);
                            log.debug("设置裂隙数据 ETA 完成");
                        }
                        catch (Exception e) {
                            log.warn("设置裂隙数据 ETA 时发生错误", e);
                        }

                        // 获取当前数据库中所有裂隙 ID
                        List<String> dbIds = fissureService.getActiveIds();
                        log.debug("当前数据库中已存在的裂隙数量：{}", dbIds.size());


                        // 找出新数据中不存在于数据库中的 ID，即新增数据
                        List<Fissure> newFissures = fissures.stream()
                                .filter(f -> !dbIds.contains(f.getId()))
                                .toList();

                        if (newFissures.isEmpty()){
                            log.debug("无新增裂隙数据");
                        } else {
                            String fissureInfo = newFissures.stream()
                                    .map(f -> f.getNode()+ f.getTier() +f.getMissionType()+"  "+f.getEta())
                                    .collect(Collectors.joining("\n"));
                            log.info("新增裂隙数据：\n{}", fissureInfo);
                        }

                        // 清空老数据
                        fissureService.clearFissures();
                        log.debug("旧数据清空完成");

                        // 插入全部获取道的数据
                        log.debug("开始插入数据，共 {} 条", fissures.size());
                        fissureService.batchInsertFissures(fissures);
                        log.debug("数据插入完成");

                        // 存储到统计表（仅新数据）与裂隙推送
                        if (!newFissures.isEmpty()) {
                            log.debug("开始统计新增裂隙数据，共 {} 条", newFissures.size());
                            fissureService.fissureStatistic(newFissures);
                            log.debug("新增裂隙数据统计完成");
                            log.debug("裂隙统计数据添加完成，新增{} 条数据", newFissures.size());
                            notifyMatchingGroups(bot, newFissures);
                        } else {
                            log.debug("无新增裂隙数据需要处理");
                        }
                    } catch (Exception e) {
                        log.error("处理裂隙数据时发生错误", e);
                    }
                })
                .exceptionally(throwable -> {
                    log.error("异步处理裂隙数据时发生错误", throwable);
                    return null;
                });
    }

    /**
     * @param bot      机器人实例，用于发送消息
     * @param fissures 新发现的裂隙列表，包含裂隙相关信息
     */
    private void notifyMatchingGroups(Bot bot, List<Fissure> fissures) {
        try {
            log.debug("开始通知匹配的群组，新增裂隙数: {}", fissures.size());
            // 获取所有裂隙任务
            List<FissureTask> allTasks = fissureService.selectAllFissureTasks();
            log.debug("获取到所有裂隙任务，共 {} 个", allTasks.size());

            // 存储群组推送数据
            Map<Long, Map<Fissure, Set<Long>>> groupPushData = new HashMap<>();
            // 存储私聊推送数据 (key: userId, value: Map<fissure, Set<userId>>)
            Map<Long, Map<Fissure, Set<Long>>> privatePushData = new HashMap<>();

            for (FissureTask task : allTasks) {
                // 判断是否为私聊任务 (groupId == userId)
                boolean isPrivateTask = task.getGroupId() == task.getUserId();

                // 根据任务类型选择对应的数据存储结构
                Map<Fissure, Set<Long>> fissureMap = isPrivateTask ?
                        privatePushData.computeIfAbsent(task.getUserId(), k -> new HashMap<>()) :
                        groupPushData.computeIfAbsent(task.getGroupId(), k -> new HashMap<>());

                for (Fissure fissure : fissures) {
                    if (!FissureTaskUtil.isValidMatch(fissure, task)) {
                        continue;
                    }

                    // 为当前裂隙获取需要@的用户集合
                    Set<Long> atUsers = fissureMap.computeIfAbsent(fissure, f -> new HashSet<>());

                    // 如果不是常驻任务，则添加用户ID并删除任务
                    if (!task.getIsPersist()) {
                        atUsers.add(task.getUserId());
                        fissureService.deleteFissureTask(task);
                    }
                }
            }

            // 处理群组推送
            if (!groupPushData.isEmpty()) {
                log.info("完成群组匹配计算，推送数据组数: {}", groupPushData.size());
                FissureTaskUtil.sendFissureMessage(bot, groupPushData);
                log.info("群组通知完成");
            } else {
                log.debug("无匹配到的群组任务");
            }

            // 处理私聊推送
            if (!privatePushData.isEmpty()) {
                log.info("完成私聊匹配计算，推送数据条数: {}", privatePushData.size());

                // 遍历所有需要推送私聊的用户
                privatePushData.forEach((userId, fissureMap) ->
                        fissureMap.forEach((fissure, userSet) -> {
                            // 构建推送消息内容
                            String messageType = Boolean.TRUE.equals(fissure.getIsHard()) ? "钢铁" : "普通";
                            String body = String.format(
                                    "[裂隙推送] %s %s %s %s\n剩余时间：%s\n",
                                    messageType,
                                    fissure.getNode(),
                                    fissure.getTier(),
                                    fissure.getMissionType(),
                                    fissure.getEta()
                            );

                            // 发送私聊消息
                            bot.sendPrivateMsg(userId, body, false);
                        })
                );

                log.info("私聊通知完成");
            } else {
                log.debug("无匹配到的私聊任务");
            }
        } catch (Exception e) {
            log.error("通知匹配群组时发生错误", e);
        }
    }

}