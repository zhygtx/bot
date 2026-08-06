package com.generalbot.ai.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/** GET 请求工具类：统一超时配置，供定时拉取等外部请求复用 */
@Slf4j
@Component
public class HttpRequestUtil {

    private final RestClient restClient;

    /** 初始化带连接/读取超时的 RestClient */
    public HttpRequestUtil() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(30));
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    /** GET 请求并返回响应体字符串；请求失败返回 null 并记录日志 */
    public String getText(String url) {
        try {
            return restClient.get().uri(url).retrieve().body(String.class);
        } catch (Exception e) {
            log.warn("GET 请求失败: {} - {}", url, e.getMessage());
            return null;
        }
    }
}
