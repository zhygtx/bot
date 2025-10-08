package com.news.web.task;

import com.news.web.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TemplatePreRenderTask {

    @Autowired
    private TemplateService templateService;

    /**
     * 定时预渲染所有模板（每1小时执行一次）
     */
    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void preRenderAllTemplates() {
        try {
            log.info("开始预渲染所有模板...");
            templateService.preRenderAllTemplates();
            log.info("所有模板预渲染完成");
        } catch (Exception e) {
            log.error("预渲染所有模板失败", e);
        }
    }
}
