package com.example.demo.initializer;

import com.example.demo.utils.HTMLUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.stereotype.Component;

/**
 * 浏览器初始化器
 */
@Slf4j
@Component
public class BrowserInitializer {

    /**
     * 延迟初始化浏览器（等待所有服务启动完成）
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initBrowserAfterDelay() {
        log.info("应用已启动，将在10秒后初始化浏览器...");
        HTMLUtil.delayedInit();
    }
}
