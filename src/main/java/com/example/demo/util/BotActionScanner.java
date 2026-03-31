package com.example.demo.util;

import com.example.demo.api.BotActionService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * BOT动作扫描器，用于自动扫描BotActionService接口的方法信息
 */
@Component
@Slf4j
public class BotActionScanner {

    @Getter
    private final ApplicationContext applicationContext;
    private final Map<String, BotActionMethodInfo> botActionMethods = new HashMap<>();

    public BotActionScanner(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        init();
    }

    /**
     * 初始化，扫描BotActionService接口的方法信息
     */
    private void init() {
        try {
            // 获取BotActionService接口
            Class<BotActionService> botActionServiceClass = BotActionService.class;

            // 扫描所有方法
            for (Method method : botActionServiceClass.getDeclaredMethods()) {
                String methodName = method.getName();
                int paramCount = method.getParameterCount();

                // 获取参数名称和类型（需要编译时保留参数名）
                String[] paramNames = new String[paramCount];
                String[] paramTypes = new String[paramCount];
                for (int i = 0; i < paramCount; i++) {
                    paramNames[i] = method.getParameters()[i].getName();
                    paramTypes[i] = method.getParameters()[i].getType().getSimpleName();
                }

                BotActionMethodInfo methodInfo = new BotActionMethodInfo(methodName, paramCount, paramNames, paramTypes);
                botActionMethods.put(methodName, methodInfo);
                log.info("扫描到BOT动作方法: {}，参数数量: {}，参数: {}", methodName, paramCount, String.join(", ", paramNames));
            }
        } catch (Exception e) {
            log.error("扫描BOT动作方法失败", e);
        }
    }

    /**
     * 获取BOT动作方法的参数数量
     * @param methodName 方法名
     * @return 参数数量
     */
    public int getMethodParamCount(String methodName) {
        BotActionMethodInfo methodInfo = botActionMethods.get(methodName);
        return methodInfo != null ? methodInfo.paramCount() : 0;
    }

    /**
     * 获取BOT动作方法的参数索引
     * @param methodName 方法名
     * @param paramName 参数名
     * @return 参数索引
     */
    public int getMethodParamIndex(String methodName, String paramName) {
        BotActionMethodInfo methodInfo = botActionMethods.get(methodName);
        if (methodInfo == null) {
            return -1;
        }
        
        String[] paramNames = methodInfo.paramNames();
        for (int i = 0; i < paramNames.length; i++) {
            if (paramNames[i].equals(paramName)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 获取所有BOT动作方法信息
     * @return BOT动作方法信息映射
     */
    public Map<String, BotActionMethodInfo> getAllBotActionMethods() {
        return botActionMethods;
    }

    /**
     * BOT 动作方法信息
     */
    public record BotActionMethodInfo(String methodName, int paramCount, String[] paramNames, String[] paramTypes) {
    
    }
}
