package com.example.demo.ai.ws;

import com.example.demo.security.UserPrincipal;
import com.example.demo.util.JWTUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

/**
 * 浏览器端 AI WebSocket 握手鉴权。
 */
@Slf4j
@Component
public class AIPluginWsAuthInterceptor implements HandshakeInterceptor {

    public static final String ATTR_USER = "aiPluginUser";

    private final JWTUtil jwtUtil;

    public AIPluginWsAuthInterceptor(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request,
                                   @NonNull ServerHttpResponse response,
                                   @NonNull WebSocketHandler wsHandler,
                                   @NonNull Map<String, Object> attributes) {
        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            reject(response, "Missing token");
            return false;
        }

        try {
            String userId = jwtUtil.getUserIdFromToken(token);
            if (!jwtUtil.isTokenInRedis(userId, token) || !jwtUtil.verifyToken(token)) {
                reject(response, "Invalid token");
                return false;
            }
            attributes.put(ATTR_USER, new UserPrincipal(
                    userId,
                    jwtUtil.getAccountFromToken(token),
                    jwtUtil.getUserNameFromToken(token)
            ));
            return true;
        } catch (Exception e) {
            log.warn("AI WebSocket 握手鉴权失败: {}", e.getMessage());
            reject(response, "Invalid token");
            return false;
        }
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request,
                               @NonNull ServerHttpResponse response,
                               @NonNull WebSocketHandler wsHandler,
                               Exception exception) {
        // no-op
    }

    private String extractToken(ServerHttpRequest request) {
        List<String> authHeaders = request.getHeaders().get("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String header = authHeaders.get(0);
            if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
                return header.substring(7);
            }
        }

        return UriComponentsBuilder.fromUri(request.getURI())
                .build()
                .getQueryParams()
                .getFirst("token");
    }

    private void reject(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        try {
            response.getBody().write(message.getBytes());
        } catch (Exception ignored) {
            // 握手已结束时忽略写入失败
        }
    }
}
