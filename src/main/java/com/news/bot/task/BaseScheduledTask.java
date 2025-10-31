package com.news.bot.task;


import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public abstract class BaseScheduledTask implements Runnable {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    // 任务ID
    @Getter
    private String taskId;
    // 任务描述
    @Getter
    private String description;
    // 执行间隔
    @Getter
    private Duration interval;

    public BaseScheduledTask(String taskId, String description, Duration interval) {
        this.taskId = taskId;
        this.description = description;
        this.interval = interval;
    }

    // 抽象方法，子类必须实现具体的任务逻辑
    public abstract void task();

    @Override
    public void run() {
        try {
            logger.info("开始执行任务: {}", getDescription());
            task();
            logger.info("任务执行完成: {}", getDescription());
        } catch (Exception e) {
            logger.error("任务执行失败: {}", getDescription(), e);
        }
    }

}
