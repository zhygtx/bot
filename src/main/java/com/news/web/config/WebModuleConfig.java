package com.news.web.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Web模块配置类
 * 负责扫描和配置Web模块相关的组件
 */
@Configuration
@ComponentScan(basePackages = "com.news.web")
public class WebModuleConfig {
    
}