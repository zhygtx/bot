package com.news.bot.wf.utils;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class HtmlToImageUtil {

    private final Browser browser;
    private final TemplateEngine templateEngine;

    // 自定义线程池
    private static final ExecutorService imageExecutor = Executors.newFixedThreadPool(20);

    public HtmlToImageUtil(Browser browser, TemplateEngine templateEngine) {
        this.browser = browser;
        this.templateEngine = templateEngine;
    }

    public CompletableFuture<String> renderToBase64Async(String templateName, Map<String, Object> variables, int width) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("开始生成图片 - 当前线程: {}", Thread.currentThread().getName());

            try (BrowserContext context = browser.newContext()) {
                Page page = context.newPage();
                page.setViewportSize(width, 1);

                Context thymeleafContext = new Context();
                thymeleafContext.setVariables(variables);
                String html = templateEngine.process(templateName, thymeleafContext);

                page.setContent(html, new Page.SetContentOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));
                byte[] imageBytes = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));

                page.close();

                String base64 = Base64.getEncoder().encodeToString(imageBytes);

                return "base64://"+base64;
            } catch (Exception e) {
                log.error("HTML 转图片失败", e);
                throw new RuntimeException("HTML 转图片失败", e);
            }
        }, imageExecutor);
    }


/**
     * 同步调用方法
     */
    public String renderToBase64(String templateName, Map<String, Object> contextData, int width) {
        return renderToBase64Async(templateName, contextData, width).join();
    }

}