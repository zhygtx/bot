package com.news.bot.infrastructure;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Component
@RequiredArgsConstructor
public class UrlService {

    private final Browser browser;     // 全局共享浏览器
    private final Executor pwExecutor; // Playwright 专用线程池

    // 随机伪装池
    private static final List<String> UA_POOL = List.of(
            // Desktop Chrome
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36",
            // Desktop Firefox
            "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:102.0) Gecko/20100101 Firefox/102.0",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:113.0) Gecko/20100101 Firefox/113.0",
            // Mobile Safari
            "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1",
            // Mobile Chrome
            "Mozilla/5.0 (Linux; Android 12; SM-G991U) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Mobile Safari/537.36"
    );
    private static final List<String> LANG_POOL = List.of(
            "zh-CN,zh;q=0.9", "en-US,en;q=0.8", "ja-JP,ja;q=0.7", "fr-FR,fr;q=0.8", "de-DE,de;q=0.8"
    );
    private static final List<String> REFERRER_POOL = List.of(
            "",                                  // 直接无 referer
            "https://www.google.com/",
            "https://www.bing.com/",
            "https://www.baidu.com/",
            "https://duckduckgo.com/"
    );
    private static final List<String> TIMEZONE_POOL = List.of(
            "Asia/Shanghai", "Asia/Tokyo", "Europe/London",
            "America/New_York", "Europe/Berlin", "Australia/Sydney"
    );
    private static final List<ViewportSize> VIEWPORT_POOL = List.of(
            new ViewportSize(1920, 1080),
            new ViewportSize(1366, 768),
            new ViewportSize(1440, 900),
            new ViewportSize(360, 640),   // 手机竖屏
            new ViewportSize(768, 1024)   // 平板
    );

    private final Random rnd = new Random();

    /**
     * 异步返回 CompletableFuture<String>
     */
    public CompletableFuture<String> fetchJsonAsync(String url) {
        return fetchJsonAsync(url, Collections.emptyMap());
    }

    /**
     * 异步返回 CompletableFuture<String>，支持自定义请求头
     */
    public CompletableFuture<String> fetchJsonAsync(String url, Map<String, String> customHeaders) {
        return CompletableFuture.supplyAsync(() -> {
            // 随机选装
            String ua       = UA_POOL.get(rnd.nextInt(UA_POOL.size()));
            String lang     = LANG_POOL.get(rnd.nextInt(LANG_POOL.size()));
            String referer  = REFERRER_POOL.get(rnd.nextInt(REFERRER_POOL.size()));
            String timezone = TIMEZONE_POOL.get(rnd.nextInt(TIMEZONE_POOL.size()));
            ViewportSize vp = VIEWPORT_POOL.get(rnd.nextInt(VIEWPORT_POOL.size()));

            // 构建基础请求头
            Map<String, String> headers = new HashMap<>(Map.of(
                    "Accept-Language", lang,
                    "Referer", referer
            ));

            // 添加自定义请求头
            if (customHeaders != null && !customHeaders.isEmpty()) {
                headers.putAll(customHeaders);
            }

            // 构建 context 选项
            Browser.NewContextOptions options = new Browser.NewContextOptions()
                    .setUserAgent(ua)
                    .setLocale(lang.split(",")[0])      // Playwright 要 locale 而非 accept-language
                    .setTimezoneId(timezone)
                    .setViewportSize(vp.getWidth(), vp.getHeight())
                    .setDeviceScaleFactor(1 + rnd.nextDouble() * 1.5) // 随机缩放 1.0~2.5
                    .setExtraHTTPHeaders(headers);

            try (BrowserContext context = browser.newContext(options);
                 Page page = context.newPage()) {
                // 导航并等待 networkidle
                Response resp = page.navigate(url, new Page.NavigateOptions()
                        .setWaitUntil(WaitUntilState.NETWORKIDLE)
                        .setTimeout(10_000));  // 10s 超时
                if (resp == null) {
                    throw new RuntimeException("Response is null");
                }
                if (resp.status() != 200) {
                    throw new RuntimeException("HTTP " + resp.status());
                }
                return resp.text();
            } catch (Exception e) {
                log.error("[伪装 UA={};lang={};ref={};tz={};vp={}×{}] 请求失败: {}",
                        ua, lang, referer, timezone, vp.getWidth(), vp.getHeight(), url, e);
                throw new RuntimeException(e);
            }
        }, pwExecutor);
    }

    /**
     * 同步调用：直接阻塞拿到结果
     */
    public String fetchJson(String url) {
        return fetchJsonAsync(url).join();
    }

    /**
     * 同步调用：直接阻塞拿到结果，支持自定义请求头
     */
    public String fetchJson(String url, Map<String, String> customHeaders) {
        return fetchJsonAsync(url, customHeaders).join();
    }


    // 用于保存宽高对
    @Getter
    private static class ViewportSize {
        private final int width;
        private final int height;
        public ViewportSize(int w, int h) { this.width = w; this.height = h; }
    }
}
