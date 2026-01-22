package com.example.demo.utils;

import com.example.demo.pojo.Result;
import com.example.demo.pojo.task.actionContent.Template;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitUntilState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import jakarta.annotation.PreDestroy;
import java.util.Base64;

@Slf4j
@Component  // 将此类注册为 Spring 组件
public class HTMLUtil {

    // Spring 注入的 ObjectMapper，替代静态实例
    private final ObjectMapper objectMapper;

    // Playwright 与 Browser 实例
    private volatile Playwright playwright;
    private volatile Browser browser;

    // 专门用于渲染动态模板字符串的 TemplateEngine
    private volatile TemplateEngine stringTemplateEngine;

    // 浏览器视口默认宽度
    private static final int DEFAULT_VIEWPORT_WIDTH = 800;
    private static final int MIN_VIEWPORT_DIM = 200; // 最小宽/高保护

    public HTMLUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        initializeResources(); // 构造时初始化资源
    }

    /**
     * 初始化所有必需资源
     */
    private void initializeResources() {
        initBrowserIfNeeded();
        initStringTemplateEngineIfNeeded();
        log.info("HTMLUtil 资源初始化完成");
    }

    // 初始化 Playwright/Browser（线程安全）
    private void initBrowserIfNeeded() {
        if (browser == null) {
            synchronized (this) {
                if (browser == null) {
                    playwright = Playwright.create();
                    browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                            .setHeadless(true)
                    );
                }
            }
        }
    }

    // 专门用于渲染动态模板字符串的 Thymeleaf（线程安全）
    private void initStringTemplateEngineIfNeeded() {
        if (stringTemplateEngine == null) {
            synchronized (this) {
                if (stringTemplateEngine == null) {
                    StringTemplateResolver resolver = new StringTemplateResolver();
                    resolver.setTemplateMode("HTML");
                    resolver.setCacheable(false); // 动态模板默认不缓存
                    TemplateEngine engine = new TemplateEngine();
                    engine.setTemplateResolver(resolver);
                    stringTemplateEngine = engine;
                }
            }
        }
    }

    /**
     * 将 jsonData 渲染为 HTML（使用传入的模板），
     * 然后用复用的 Playwright Browser 截图并返回 PNG 的 Base64 字符串。
     *
     * @param jsonData JSON 数据
     * @param template 模板实体类
     * @return PNG 的 Base64 编码字符串
     */
    public Result<String> jsonToImage(String jsonData, Template template) {
        // 1. 解析 JSON
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(jsonData);
        } catch (Exception e) {
            log.error("JSON 解析失败", e);
            return Result.error("JSON 解析失败: " + e.getMessage());
        }

        // 2. 渲染模板
        String html;
        try {
            Context ctx = new Context();
            ctx.setVariable("data", jsonNode);
            html = stringTemplateEngine.process(template.getContent(), ctx);
        } catch (Exception e) {
            log.error("渲染模板失败", e);
            return Result.error("渲染模板失败: " + e.getMessage());
        }

        return getString(template, html);
    }

    /**
     * 将 data 对象渲染为 HTML（使用传入的模板），
     * 然后用复用的 Playwright Browser 截图并返回 PNG 的 Base64 字符串。
     *
     * @param data     数据对象
     * @param template 模板实体类
     * @return PNG 的 Base64 编码字符串
     */
    public Result<String> objectToImage(Object data, Template template) {
        String html;
        try {
            Context ctx = new Context();
            ctx.setVariable("data", data);
            html = stringTemplateEngine.process(template.getContent(), ctx);
        } catch (Exception e) {
            log.error("渲染模板失败", e);
            return Result.error("渲染模板失败: " + e.getMessage());
        }

        return getString(template, html);
    }

    /**
     * 获取 PNG 的 Base64 编码字符串。
     *
     * @param template 模板实体类
     * @param html     HTML 内容
     * @return PNG 的 Base64 编码字符串
     */
    public Result<String> getString(Template template, String html) {
        byte[] pngBytes;
        try (Page page = browser.newPage()) {
            int initialWidth = Math.max(MIN_VIEWPORT_DIM,
                    template.getWidth() > 0 ? template.getWidth() : DEFAULT_VIEWPORT_WIDTH);
            int initialHeight = Math.max(MIN_VIEWPORT_DIM,
                    template.getHeight() > 0 ? template.getHeight() : MIN_VIEWPORT_DIM);
            page.setViewportSize(initialWidth, initialHeight);

            page.setContent(html, new Page.SetContentOptions()
                    .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

            page.waitForFunction("() => { " +
                    "const images = Array.from(document.images); " +
                    "return images.every(img => img.complete); " +
                    "}");

            Number heightNumber = (Number) page.evaluate("() => document.body.scrollHeight");
            int contentHeight = (heightNumber == null) ? initialHeight :
                    Math.max(200, heightNumber.intValue());

            page.setViewportSize(initialWidth, contentHeight);

            pngBytes = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
        } catch (Exception e) {
            log.error("使用 Playwright 截图失败", e);
            return Result.error("使用 Playwright 截图失败: " + e.getMessage());
        }

        return Result.success(Base64.getEncoder().encodeToString(pngBytes));
    }

    /**
     * 应用关闭时清理资源
     */
    @PreDestroy
    public void cleanup() {
        log.info("正在清理 HTMLUtil 资源...");

        if (browser != null) {
            try {
                browser.close();
            } catch (Exception e) {
                log.error("关闭浏览器失败", e);
            }
        }

        if (playwright != null) {
            try {
                playwright.close();
            } catch (Exception e) {
                log.error("关闭 Playwright 失败", e);
            }
        }

        log.info("HTMLUtil 资源清理完成");
    }
}