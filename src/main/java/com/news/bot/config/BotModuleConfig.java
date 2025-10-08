package com.news.bot.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 机器人模块配置类
 * 负责扫描和配置机器人模块相关的组件
 */
@Configuration
@ComponentScan(basePackages = "com.news.bot")
public class BotModuleConfig {
    
}