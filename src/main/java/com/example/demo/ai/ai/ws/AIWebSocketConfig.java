package com.example.demo.ai.ai.ws;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * AI 插件生成 WebSocket 配置。
 */
@Configuration
@EnableWebSocket
public class AIWebSocketConfig implements WebSocketConfigurer {

    public static final String PATH = "/ws/ai-plugin";

    private final AIWebSocketHandler handler;
    private final AIWsAuthInterceptor authInterceptor;

    public AIWebSocketConfig(AIWebSocketHandler handler,
                             AIWsAuthInterceptor authInterceptor) {
        this.handler = handler;
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, PATH)
                .addInterceptors(authInterceptor)
                .setAllowedOriginPatterns("*");
    }
}
