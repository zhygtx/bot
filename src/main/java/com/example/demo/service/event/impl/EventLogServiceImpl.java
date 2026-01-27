package com.example.demo.service.event.impl;

import com.example.demo.mapper.event.EventLogMapper;
import com.example.demo.pojo.event.EventLog;
import com.example.demo.service.event.EventLogService;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class EventLogServiceImpl implements EventLogService {

    private final EventLogMapper eventLogMapper;

    // 内存队列缓存日志
    private final Queue<EventLog> logQueue = new ConcurrentLinkedQueue<>();
    // 日志计数器
    private final AtomicInteger count = new AtomicInteger(0);
    // 批量大小阈值
    private static final int BATCH_SIZE = 100;
    // 是否正在处理批量插入
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);

    public EventLogServiceImpl(EventLogMapper eventLogMapper) {
        this.eventLogMapper = eventLogMapper;
    }

    /**
     * 添加事件日志到队列
     * @param event 事件
     */
    @Override
    public void insert(EventLog event) {
        logQueue.offer(event);
        int currentCount = count.incrementAndGet();

        // 达到批量大小且未在处理时触发批量插入
        if (currentCount >= BATCH_SIZE && !isProcessing.get()) {
            processBatchInsert();
        }
    }

    /**
     * 根据群组和类型获取事件日志
     * @param group 群组ID
     * @param type 事件类型
     * @return 事件日志列表
     */
    @Override
    public List<EventLog> getEventLogs(Long group, String type){
        return eventLogMapper.getByGroupAndType(group, type);
    }

    /**
     * 定时任务：每分钟检查并处理日志
     */
    @Scheduled(fixedRate = 1000) // 60秒 = 1分钟
    public void scheduledProcess() {
        if (count.get() > 0 && !isProcessing.get()) {
            processBatchInsert();
        }
    }

    /**
     * 应用关闭前处理剩余日志
     */
    @PreDestroy
    public void flushRemainingLogs() {
        // 处理所有剩余的日志，使用同步方式确保完成
        while (count.get() > 0) {
            processBatchInsert(); // 使用同步版本
        }
    }

    /**
     * 批量插入日志
     */
    @Async
    public void processBatchInsert() {
        if (!isProcessing.compareAndSet(false, true)) {
            return;
        }

        try {
            List<EventLog> batchLogs = new ArrayList<>();
            EventLog eventLog;

            // 取出最多 BATCH_SIZE 条记录
            int itemsToTake = Math.min(count.get(), BATCH_SIZE);
            for (int i = 0; i < itemsToTake; i++) {
                eventLog = logQueue.poll();
                if (eventLog != null) {
                    batchLogs.add(eventLog);
                }
            }

            if (!batchLogs.isEmpty()) {
                // 批量插入数据库
                eventLogMapper.insert(batchLogs);
                // 原子性地减少计数器
                count.addAndGet(-batchLogs.size());
            }
        } finally {
            isProcessing.set(false);
        }
    }

}
