package com.example.demo.cache;

import com.example.demo.pojo.task.Scope;
import com.example.demo.service.task.ScopeService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

@Slf4j
@Component
public class ScopeCacheManager {

    private final ScopeService scopeService;

    @Autowired
    public ScopeCacheManager(ScopeService scopeService) {
        this.scopeService = scopeService;
    }

    // 提供外部访问缓存的方法
    // 使用线程安全的集合存储缓存
    @Getter
    private volatile Set<Scope> cachedScopes = Collections.emptySet();

    // 定时刷新Scope缓存
    @Scheduled(fixedDelay = 60_000, initialDelay = 10_000)
    public void refreshCache() {
        try {
            this.cachedScopes = scopeService.getAllScopes();
            log.info("Scope缓存已更新");
        } catch (Exception e) {
            // 记录日志，但不中断定时任务
            log.error("缓存更新失败: ", e);
        }
    }

}