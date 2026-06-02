package com.example.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 工作流执行器配置类
 * 提供全局线程池和并发限流能力
 */
@Configuration
@Slf4j
public class WorkflowExecutorConfig {

    @Value("${workflow.executor.core-pool-size:8}")
    private int corePoolSize;

    @Value("${workflow.executor.max-pool-size:16}")
    private int maxPoolSize;

    @Value("${workflow.executor.queue-capacity:200}")
    private int queueCapacity;

    @Value("${workflow.executor.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    @Value("${workflow.executor.max-concurrent:16}")
    private int maxConcurrent;

    private static final AtomicInteger counter = new AtomicInteger(0);

    /**
     * 工作流执行线程池
     * 用于执行工作流实例
     */
    @Bean(name = "workflowExecutor")
    public ExecutorService workflowExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveSeconds,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                r -> {
                    Thread thread = new Thread(r, "workflow-exec-" + counter.incrementAndGet());
                    thread.setDaemon(true);
                    return thread;
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        
        log.info("工作流线程池初始化完成 - 核心线程数:{}, 最大线程数:{}, 队列容量:{}", 
                corePoolSize, maxPoolSize, queueCapacity);
        return executor;
    }

    /**
     * 并发限流信号量
     * 控制同时执行的工作流最大数量
     */
    @Bean(name = "workflowSemaphore")
    public Semaphore workflowSemaphore() {
        log.info("工作流并发信号量初始化完成 - 最大并发数:{}", maxConcurrent);
        return new Semaphore(maxConcurrent);
    }
}
