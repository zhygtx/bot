package com.example.demo.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 认证相关工具类
 */
@Component
public class AuthUtil {

    private final JWTUtil jwtUtil;

    @Autowired
    public AuthUtil(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 从请求头中获取用户ID
     * @param request HTTP请求
     * @return 用户ID，如果获取失败返回null
     */
    public String getCurrentUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                return jwtUtil.getUserIdFromToken(token);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 从请求头中获取用户账号
     * @param request HTTP请求
     * @return 用户账号，如果获取失败返回null
     */
    public String getCurrentUserAccount(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                return jwtUtil.getAccountFromToken(token);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}