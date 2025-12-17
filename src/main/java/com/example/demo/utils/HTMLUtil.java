package com.example.demo.utils;

import com.gbx.warframe.worldstate.pojo.Fissure;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitUntilState;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
public class HTMLUtil {

    // Playwright 与 Browser 单例（延迟初始化）
    private static volatile Playwright PLAYWRIGHT;
    private static volatile Browser BROWSER;

    // Thymeleaf TemplateEngine 单例
    private static volatile TemplateEngine TEMPLATE_ENGINE;

    // 模板在 classpath 下的位置（resources/templates/fissure.html）

    // 浏览器视口大小（可按需调整）
    private static final int VIEWPORT_WIDTH = 800;

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

    // 初始化 Thymeleaf（线程安全）
    private static void initTemplateEngineIfNeeded() {
        if (TEMPLATE_ENGINE == null) {
            synchronized (HTMLUtil.class) {
                if (TEMPLATE_ENGINE == null) {
                    ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
                    resolver.setPrefix("templates/"); // classpath: templates/
                    resolver.setSuffix(".html");
                    resolver.setTemplateMode("HTML");
                    resolver.setCharacterEncoding("UTF-8");
                    resolver.setCacheable(true); // 开发时 false，生产可改为 true

                    TemplateEngine engine = new TemplateEngine();
                    engine.setTemplateResolver(resolver);
                    TEMPLATE_ENGINE = engine;
                }
            }
        }
    }

    /**
     * 将 fissureMap 渲染为 HTML（使用 classpath 下的 templates/fissure.html），
     * 然后用复用的 Playwright Browser 截图并返回 PNG 的 Base64 字符串。
     *
     * @param fissureMap Map<String, List<Fissure>> （你的项目中已有 Fissure 实体）
     * @return PNG 的 Base64 编码字符串
     */
    public static String toImage(Map<String, List<Fissure>> fissureMap) {
        // 1. 初始化单例资源
        initTemplateEngineIfNeeded();
        initBrowserIfNeeded();

        // 2. 渲染模板（使用 Thymeleaf，从 classpath 的 templates/fissure.html）
        String html;
        try {
            // 读取模板内容（可选：直接使用 TEMPLATE_ENGINE.process(templateName, ctx)）
            // 这里直接用模板文件名渲染，传入变量 data 为 entrySet
            Context ctx = new Context();
            ctx.setVariable("data", fissureMap.entrySet());
            // 模板解析器已配置 prefix/suffix，所以传入模板名不带路径与后缀
            html = TEMPLATE_ENGINE.process("fissure", ctx);
        } catch (Exception e) {
            throw new RuntimeException("渲染模板失败", e);
        }

        // 3. 使用复用的 Browser 创建 Page、加载 HTML 并截图使用动态视口大小
        byte[] pngBytes;
        try (Page page = BROWSER.newPage()) {
            // 先设置宽度，高度设为较大值确保内容完整显示
            page.setViewportSize(VIEWPORT_WIDTH, 2000);

            /*
            DOMCONTENTLOADED: 等待 DOM 内容加载完成（类似浏览器的 DOMContentLoaded 事件）HTML 文档完全加载和解析完毕不等待样式表、图像等资源加载
            LOAD: 等待所有资源加载完成（类似浏览器的 load 事件）DOM 内容加载完成所有依赖资源（如样式表、脚本、图像）也加载完成
            NETWORKIDLE: 等待网络空闲至少500ms内没有网络连接活动等待时间最长，但最安全
             */
            // 加载HTML内容
            page.setContent(html, new Page.SetContentOptions()
                    .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

            // 获取实际内容高度
            Integer contentHeight = (Integer) page.evaluate("document.body.scrollHeight");

            // 重新设置准确的视口大小
            page.setViewportSize(VIEWPORT_WIDTH, contentHeight);

            // 截图（整页）
            pngBytes = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
        } catch (Exception e) {
            throw new RuntimeException("使用 Playwright 截图失败", e);
        }

        // 4. 转 Base64 并返回
        return Base64.getEncoder().encodeToString(pngBytes);
    }
}