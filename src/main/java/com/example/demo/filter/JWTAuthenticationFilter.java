package com.example.demo.filter;

import com.example.demo.utils.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JWTAuthenticationFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(@Nullable HttpServletRequest request,
                                    @Nullable HttpServletResponse response,
                                    @Nullable FilterChain filterChain) throws ServletException, IOException {
        if (request == null || response == null || filterChain == null) {
            return;
        }

        response.setContentType("application/json;charset=UTF-8");

        // 排除不需要认证的接口
        String requestURI = request.getRequestURI();
        if (requestURI.equals("/user/login") || requestURI.equals("/user/insertUser") || requestURI.startsWith("/email/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 获取Authorization头
        String authorizationHeader = request.getHeader("Authorization");

        // 检查令牌格式
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            PrintWriter writer = response.getWriter();
            writer.write("{\"code\": 1, \"message\": \"未提供有效的认证令牌\"}");
            writer.flush();
            writer.close();
            return;
        }

        // 提取令牌
        String token = authorizationHeader.substring(7);

        // 验证令牌
        // 修改验证逻辑
        if (!jwtUtil.verifyToken(token)) {
            // 返回401错误
            PrintWriter writer = response.getWriter();
            writer.write("{\"code\": 1, \"message\": \"认证令牌无效或已过期\"}");
            writer.flush();
            writer.close();
            return;
        }

        // 从token中获取userId
        String userId = jwtUtil.getUserIdFromToken(token);

        // 检查token是否在Redis中存在
        if (!jwtUtil.isTokenInRedis(userId, token)) {
            // 返回401错误
            PrintWriter writer = response.getWriter();
            writer.write("{\"code\": 1, \"message\": \"认证令牌已被注销\"}");
            writer.flush();
            writer.close();
            return;
        }

        // 令牌有效，继续处理请求
        filterChain.doFilter(request, response);
    }
}
