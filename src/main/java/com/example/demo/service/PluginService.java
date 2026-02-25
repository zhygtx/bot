package com.example.demo.service;

import com.example.demo.pojo.plugin.PluginInfo;

public interface PluginService {
    /**
     * 分析插件JAR包并生成PluginInfo对象
     * @param pluginInfo 插件信息
     * @return PluginInfo对象
     */
    PluginInfo analyzePlugin(PluginInfo pluginInfo);

    PluginInfo test();

}
