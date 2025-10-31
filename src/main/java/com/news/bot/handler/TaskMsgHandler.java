package com.news.bot.handler;

import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.PrivateMessageHandler;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.news.bot.task.TaskManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;

@Component
@Slf4j
@Shiro
public class TaskMsgHandler {

    @Autowired
    private TaskManager taskManager;

    @PrivateMessageHandler
    @MessageHandlerFilter(cmd = "(启动|停止)\\s+(\\S+)", senders = {1874743565})
    public void taskControl(Bot bot, PrivateMessageEvent event, Matcher matcher){
        String action = matcher.group(1);
        String taskId = matcher.group(2);
        try {
            if ("启动".equals(action)) {
                taskManager.startTask(taskId);
                bot.sendPrivateMsg(event.getUserId(), "任务 [" + taskId + "] 已启动", false);
            } else if ("停止".equals(action)) {
                taskManager.stopTask(taskId);
                bot.sendPrivateMsg(event.getUserId(), "任务 [" + taskId + "] 已停止", false);
            }
        } catch (Exception e) {
            log.error("任务控制失败：{}", e.getMessage());
            bot.sendPrivateMsg(event.getUserId(), "操作失败：" + e.getMessage(), false);
        }
    }

    @PrivateMessageHandler
    @MessageHandlerFilter(cmd = "任务状态", senders = {1874743565})
    public void viewTask(Bot bot, PrivateMessageEvent event){
        Map<String, Boolean> status = taskManager.getAllTaskStatus();
        StringBuilder message = new StringBuilder("任务状态:\n");
        for (Map.Entry<String, Boolean> entry : status.entrySet()) {
            message.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue() ? "运行中" : "已停止")
                    .append("\n");
        }
        bot.sendPrivateMsg(event.getUserId(), message.toString(), false);
    }
}
