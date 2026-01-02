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
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.util.Base64;

@Slf4j
public class HTMLUtil {

    // Playwright 与 Browser 单例（延迟初始化）
    private static volatile Playwright PLAYWRIGHT;
    private static volatile Browser BROWSER;

    //专门用于渲染动态模板字符串（来自 DB/外部）的 TemplateEngine
    private static volatile TemplateEngine STRING_TEMPLATE_ENGINE;

     // Jackson ObjectMapper 单例（线程安全，可复用）
     private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

     //浏览器视口默认宽度（可按需调整）
     private static final int DEFAULT_VIEWPORT_WIDTH = 800;
     private static final int MIN_VIEWPORT_DIM = 200; // 最小宽/高保护

    // 初始化 Playwright/Browser（线程安全）
    private static void initBrowserIfNeeded() {
        if (BROWSER == null) {
            synchronized (HTMLUtil.class) {
                if (BROWSER == null) {
                    PLAYWRIGHT = Playwright.create();
                    BROWSER = PLAYWRIGHT.chromium().launch(new BrowserType.LaunchOptions()
                            .setHeadless(true)
                    );
                    // 可选：在 JVM 退出时关闭浏览器与 Playwright
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        try {
                            if (BROWSER != null) BROWSER.close();
                        } catch (Exception ignored) {}
                        try {
                            if (PLAYWRIGHT != null) PLAYWRIGHT.close();
                        } catch (Exception ignored) {}
                    }));
                }
            }
        }
    }

    // 专门用于渲染动态模板字符串的 Thymeleaf（线程安全）
    private static void initStringTemplateEngineIfNeeded(){
        if (STRING_TEMPLATE_ENGINE == null){
            synchronized (HTMLUtil.class) {
                if (STRING_TEMPLATE_ENGINE == null) {
                    StringTemplateResolver resolver = new StringTemplateResolver();
                    resolver.setTemplateMode("HTML");
                    resolver.setCacheable(false); // 动态模板默认不缓存；可按模板 id/hash 缓存
                    TemplateEngine engine = new TemplateEngine();
                    engine.setTemplateResolver(resolver);
                    STRING_TEMPLATE_ENGINE = engine;
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
    public static Result<String> jsonToImage(String jsonData, Template template) {
        // 1.初始化资源
        initStringTemplateEngineIfNeeded();

        // 2. 将 JSON 解析为 Jackson 的树模型 JsonNode
        JsonNode jsonNode;
        try {
            // readTree 会把 JSON 解析为 JsonNode，适合动态访问任意结构
            jsonNode = OBJECT_MAPPER.readTree(jsonData);
             } catch (Exception e) {
             log.error("JSON 解析失败", e);
             return Result.error("JSON 解析失败" + e.getMessage());
        }

        // 3. 渲染模板
        String html;
        try {
            // 这里直接用模板文件名渲染，传入变量 data 为 jsonNode
            Context ctx = new Context();
            ctx.setVariable("data", jsonNode);
            // 模板解析器已配置 prefix/suffix，所以传入模板名不带路径与后缀
            html = STRING_TEMPLATE_ENGINE.process(template.getContent(), ctx);
        } catch (Exception e) {
            log.error("渲染模板失败", e);
            return Result.error("渲染模板失败" + e.getMessage());
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
    public static Result<String> objectToImage(Object data, Template template) {
        // 1. 初始化单例资源
        initStringTemplateEngineIfNeeded();
        // 2. 渲染模板（使用 Thymeleaf）
        String html;
        try {
            // 读取模板内容（可选：直接使用 TEMPLATE_ENGINE.process(templateName, ctx)）
            // 这里直接用模板文件名渲染，传入变量 data 为 entrySet
            Context ctx = new Context();
            ctx.setVariable("data", data);
            // 模板解析器已配置 prefix/suffix，所以传入模板名不带路径与后缀
            html = STRING_TEMPLATE_ENGINE.process(template.getContent(), ctx);
        } catch (Exception e) {
            throw new RuntimeException("渲染模板失败", e);
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
    private static Result<String> getString(Template template, String html) {
        // 1. 初始化单例资源
        initBrowserIfNeeded();

        // 3. 使用复用的 Browser 创建 Page、加载 HTML 并截图使用动态视口大小
        byte[] pngBytes;
        try (Page page = BROWSER.newPage()) {
            //保证最小宽高正确
            int initialWidth = Math.max(MIN_VIEWPORT_DIM, template.getWidth() > 0 ? template.getWidth() : DEFAULT_VIEWPORT_WIDTH);
            int initialHeight = Math.max(MIN_VIEWPORT_DIM, template.getHeight() > 0 ? template.getHeight() : MIN_VIEWPORT_DIM);
            page.setViewportSize(initialWidth, initialHeight);

            /*
            DOMCONTENTLOADED: 等待 DOM 内容加载完成（类似浏览器的 DOMContentLoaded 事件）HTML 文档完全加载和解析完毕不等待样式表、图像等资源加载
            LOAD: 等待所有资源加载完成（类似浏览器的 load 事件）DOM 内容加载完成所有依赖资源（如样式表、脚本、图像）也加载完成
            NETWORKIDLE: 等待网络空闲至少500ms内没有网络连接活动等待时间最长，但最安全
             */
            // 加载HTML内容
            page.setContent(html, new Page.SetContentOptions()
                    .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

            page.waitForFunction("() => { " +
                    "const images = Array.from(document.images); " +
                    "return images.every(img => img.complete); " +
                    "}");

            // 获取实际内容高度
            int contentHeight;

             //evaluate 返回 Number（可能是 Double），安全转换为int
            Number heightNumber = (Number) page.evaluate("() => document.body.scrollHeight");
            contentHeight = (heightNumber == null) ? initialHeight : Math.max(200, heightNumber.intValue());
            // 重新设置视口高度为实际内容高度
            page.setViewportSize(initialWidth, contentHeight);

            // 截图（整页）
            pngBytes = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
        } catch (Exception e) {
            log.error("使用 Playwright 截图失败", e);
            return Result.error("使用 Playwright 截图失败" + e.getMessage());
        }

        // 4. 转 Base64 并返回
        return Result.success(Base64.getEncoder().encodeToString(pngBytes));
    }
}