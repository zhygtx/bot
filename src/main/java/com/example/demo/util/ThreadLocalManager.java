package com.example.demo.util;

import java.util.HashMap;
import java.util.Map;

/**
 * ThreadLocal管理工具类，用于安全管理ThreadLocal变量
 */
public class ThreadLocalManager {
    // 方法实例缓存
    private static final ThreadLocal<Map<String, Object>> methodInstanceCache = 
        ThreadLocal.withInitial(HashMap::new);
    
    // 执行上下文缓存
    private static final ThreadLocal<Map<String, Object>> executionContext = 
        ThreadLocal.withInitial(HashMap::new);
    
    /**
     * 获取方法实例缓存
     * @return 方法实例缓存
     */
    public static Map<String, Object> getMethodInstanceCache() {
        return methodInstanceCache.get();
    }
    
    /**
     * 获取执行上下文
     * @return 执行上下文
     */
    public static Map<String, Object> getExecutionContext() {
        return executionContext.get();
    }
    
    /**
     * 清理所有ThreadLocal变量
     */
    public static void clear() {
        methodInstanceCache.remove();
        executionContext.remove();
    }
}
