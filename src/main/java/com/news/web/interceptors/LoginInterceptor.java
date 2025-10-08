package com.news.web.interceptors;

import com.news.web.utils.JwtUtil;
import com.news.web.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 登录拦截器
 * 用于验证用户请求的token有效性，并刷新token过期时间
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    /**
     * token过期时间（分钟）
     */
    private static final long TOKEN_EXPIRE_MINUTES = 60;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 请求预处理方法
     * 验证token有效性，解析用户信息并存储到ThreadLocal中
     *
     * @param request  HTTP请求对象
     * @param response HTTP响应对象
     * @param handler  处理器对象
     * @return 验证通过返回true，否则返回false
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        // 获取请求头中的token
        String token = request.getHeader("Authorization");

        // token为空直接返回401
        if (token == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        try {
            // 从 Redis 中获取相同的 token
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            String redisToken = operations.get(token);

            if (redisToken == null) {
                // token在redis中不存在，说明已过期或无效
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

            // 解析 JWT 获取用户信息
            Map<String, Object> claims = JwtUtil.parseToken(token);

            // 刷新 Token 过期时间（自动延长生存周期）
            operations.set(token, token, TOKEN_EXPIRE_MINUTES, TimeUnit.MINUTES);

            // 把业务数据存储到 ThreadLocal 中
            ThreadLocalUtil.set(claims);

            // 放行
            return true;
        } catch (Exception e) {
            // JWT解析失败或其他异常，返回401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }

    /**
     * 请求完成后清理资源
     * 清空ThreadLocal中的数据，防止内存泄漏
     *
     * @param request  HTTP请求对象
     * @param response HTTP响应对象
     * @param handler  处理器对象
     * @param ex       异常对象
     */
    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,@NonNull HttpServletResponse response,@NonNull Object handler, Exception ex){
        // 清空 ThreadLocal 中的数据
        ThreadLocalUtil.remove();
    }
}