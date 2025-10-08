package com.news;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication(scanBasePackages = {"com.news.web", "com.news.bot"})
@EnableScheduling
@EnableAsync
@EnableCaching // 启用缓存
public class NewsApplication {
    public static void main(String[] args) {
        SpringApplication.run(NewsApplication.class,args);
    }
}