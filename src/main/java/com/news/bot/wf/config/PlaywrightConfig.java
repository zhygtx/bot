package com.news.bot.wf.config;

import com.microsoft.playwright.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * 把 playwright.* 配置和 pwExecutor Bean 都集中在这里管理
 */
@Configuration
@ConfigurationProperties(prefix = "playwright")
@Getter
@Setter
@EnableAsync
@Slf4j
public class PlaywrightConfig {

    /**
     * 最大并发线程数
     */
    private int poolSize;

    /**
     * 是否无头模式
     */
    private boolean headless;

    /**
     * 页面导航和等待网络空闲的超时时间，单位毫秒
     */
    private int timeoutMs;

    private Playwright playwright;
    private Browser browser;

    /**
     * 定义一个名为 pwExecutor 的线程池 Bean
     * 并发运行 Playwright 任务时使用它
     */
    @Bean("pwExecutor")
    public Executor pwExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(poolSize);
        exec.setMaxPoolSize(poolSize * 2);
        exec.setQueueCapacity(poolSize * 10);
        exec.setThreadNamePrefix("pw-exec-");
        exec.initialize();
        return exec;
    }

    /**
     * 创建 Playwright 实例（单例）
     */
    @PostConstruct
    public void init() {
        playwright = Playwright.create();

        // 初始化浏览器
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(headless)
                        .setArgs(List.of("--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage"))
        );
        log.info("Playwright Chromium 已启动");
    }

    /**
     * 提供浏览器实例供其他组件注入使用
     */
    @Bean
    public Browser browser() {
        return browser;
    }

    /**
     * 提供 Playwright 实例供其他组件注入使用
     */
    @Bean
    public Playwright playwright() {
        return playwright;
    }

    /**
     * 应用关闭前执行资源清理
     */
    @PreDestroy
    public void closeResources() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
        log.info("Playwright 和浏览器资源已释放");
    }
}