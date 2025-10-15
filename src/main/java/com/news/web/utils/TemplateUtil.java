package com.news.web.utils;

import java.util.HashMap;
import java.util.Map;

public class TemplateUtil {

    /**
     * 预定义的模板名称列表
     */
    public static final Map<String, String> TEMPLATE_NAMES = new HashMap<>() {{
        put("fissure", "裂隙模板");
        put("fissure_test", "测试裂隙模板");
    }};
}
