package com.news.bot.config;

import com.news.bot.task.BaseScheduledTask;
import com.news.bot.task.TaskManager;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;


import java.util.List;

@Configuration
public class TaskConfiguration {

    @Autowired
    private TaskManager taskManager;

    @Autowired
    private List<BaseScheduledTask> tasks;

    @PostConstruct
    public void registerAllTasks() {
        for (BaseScheduledTask task : tasks) {
            taskManager.registerTask(
                    task.getTaskId(),
                    task,
                    task.getInterval()
            );
        }
    }
}
