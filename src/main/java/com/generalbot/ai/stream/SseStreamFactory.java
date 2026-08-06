package com.generalbot.ai.stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SseStream 工厂：统一封装 SseStream 创建逻辑。
 *
 * <p>SseStream 本身持有请求级的 SseEmitter，无法交给 Spring 代理，
 * 因此通过工厂注入单例的 ObjectMapper，再按需创建 SseStream 实例，
 * 调用方不再直接依赖 ObjectMapper。</p>
 */
@Component
public class SseStreamFactory {

    private final ObjectMapper objectMapper;

    public SseStreamFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SseStream create(SseEmitter emitter) {
        return new SseStream(emitter, objectMapper);
    }
}
