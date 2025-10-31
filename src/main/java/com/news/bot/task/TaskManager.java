package com.news.bot.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;


@Slf4j
@Component
public class TaskManager {

    @Autowired
    private TaskScheduler taskScheduler;

    // 存储所有已注册的任务
    private final Map<String, TaskInfo> registeredTasks = new ConcurrentHashMap<>();
    // 存储正在运行的任务
    private final Map<String, ScheduledFuture<?>> runningTasks = new ConcurrentHashMap<>();

    /**
     * 注册一个新的定时任务
     */
    public void registerTask(String taskId, Runnable task, Duration interval) {
        registeredTasks.put(taskId, new TaskInfo(task, interval));
    }

    /**
     * 启动指定任务
     */
    public void startTask(String taskId) {
        if (!runningTasks.containsKey(taskId) && registeredTasks.containsKey(taskId)) {
            TaskInfo taskInfo = registeredTasks.get(taskId);
            ScheduledFuture<?> future = taskScheduler.scheduleWithFixedDelay(
                    taskInfo.task(),
                    taskInfo.interval()
            );
            runningTasks.put(taskId, future);
            log.info("任务已启动：{}", taskId);
        }
    }

    /**
     * 停止指定任务
     */
    public void stopTask(String taskId) {
        ScheduledFuture<?> future = runningTasks.remove(taskId);
        if (future != null) {
            future.cancel(false);
        }
        log.info("任务已停止：{}", taskId);
    }

    /**
     * 获取所有任务状态
     */
    public Map<String, Boolean> getAllTaskStatus() {
        Map<String, Boolean> status = new HashMap<>();
        for (String taskId : registeredTasks.keySet()) {
            status.put(taskId, runningTasks.containsKey(taskId));
        }
        return status;
    }

    // 任务信息内部类
        private record TaskInfo(Runnable task, Duration interval) {
    }
}
