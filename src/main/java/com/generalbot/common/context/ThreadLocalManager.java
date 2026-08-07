package com.generalbot.common.context;

/**
 * ThreadLocal 管理工具类，仅保留插件 SQLService 需要的 userId/pluginId 隔离。
 */
public class ThreadLocalManager {

    private static final ThreadLocal<String> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> PLUGIN_ID = new ThreadLocal<>();

    private ThreadLocalManager() {
    }

    /**
     * 设置当前用户ID
     * @param userId 用户ID
     */
    public static void setUserId(String userId) {
        USER_ID.set(userId);
    }

    /**
     * 获取当前用户ID
     * @return 用户ID
     */
    public static String getUserId() {
        return USER_ID.get();
    }

    /**
     * 设置当前插件ID
     * @param pluginId 插件ID
     */
    public static void setPluginId(String pluginId) {
        PLUGIN_ID.set(pluginId);
    }

    /**
     * 获取当前插件ID
     * @return 插件ID
     */
    public static String getPluginId() {
        return PLUGIN_ID.get();
    }

    /**
     * 清理全部线程局部变量
     */
    public static void clear() {
        USER_ID.remove();
        PLUGIN_ID.remove();
    }
}
