package com.example.demo.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自定义模板渲染工具类
 * - 支持 {{...}} 语法
 * - 支持 {{expr|默认值}} 语法（如果表达式存在则输出表达式值，否则输出默认值）
 * - 未命中且无默认值时输出 "null"
 */
@Component
public class StringTemplateUtil {

    private final ObjectMapper mapper;

    // 模板缓存，提高性能
    private final Map<String, TemplateInfo> templateCache = new ConcurrentHashMap<>();

    // 正则表达式模式
    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\{\\{\\s*([^}]+?)\\s*}}");

    public StringTemplateUtil(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 渲染模板
     * @param template 模板字符串，使用 {{...}} 语法
     * @param entity 实体对象（POJO/Map/JsonNode），可为 null
     * @param extractTxt 提取文本列表（用于 valueN），可为 null
     * @return 渲染结果
     */
    public String render(String template, Object entity, List<String> extractTxt) {
        if (template == null || !template.contains("{{")) {
            return template;
        }

        // 获取缓存的模板信息，如果没有则解析并缓存
        TemplateInfo templateInfo = templateCache.computeIfAbsent(template, this::parseTemplate);

        // 构建属性映射
        Map<String, Object> model = buildModel(entity, extractTxt);

        // 渲染模板
        return renderTemplate(templateInfo, model);
    }

    /**
     * 解析模板，提取所有占位符信息
     */
    private TemplateInfo parseTemplate(String template) {
        Matcher matcher = TEMPLATE_PATTERN.matcher(template);
        StringBuilder result = new StringBuilder();
        int lastEnd = 0;
        java.util.List<Placeholder> placeholders = new java.util.ArrayList<>();

        while (matcher.find()) {
            // 添加非占位符部分
            result.append(template, lastEnd, matcher.start());

            String content = matcher.group(1).trim();
            String expression;
            String defaultValue = "null"; // 默认值

            // 检查是否有默认值
            int pipeIndex = content.indexOf('|');
            if (pipeIndex > 0) {
                expression = content.substring(0, pipeIndex).trim();
                defaultValue = content.substring(pipeIndex + 1).trim();
            } else {
                expression = content;
            }

            // 记录占位符信息
            Placeholder placeholder = new Placeholder(
                    result.length(),
                    expression,
                    defaultValue,
                    matcher.end() - matcher.start()
            );
            placeholders.add(placeholder);

            // 添加占位符标记（后续会被替换）
            result.append("\0"); // 使用空字符作为占位符标记

            lastEnd = matcher.end();
        }

        // 添加剩余部分
        result.append(template, lastEnd, template.length());

        return new TemplateInfo(result.toString(), placeholders);
    }

    /**
     * 根据模板信息和模型数据渲染模板
     */
    private String renderTemplate(TemplateInfo templateInfo, Map<String, Object> model) {
        String baseTemplate = templateInfo.template;
        List<Placeholder> placeholders = templateInfo.placeholders;

        // 从后往前替换，避免索引偏移
        StringBuilder result = new StringBuilder(baseTemplate);

        for (int i = placeholders.size() - 1; i >= 0; i--) {
            Placeholder placeholder = placeholders.get(i);

            // 获取属性值
            Object value = model.get(placeholder.expression);
            String replacement;

            if (value != null) {
                replacement = value.toString();
            } else {
                replacement = placeholder.defaultValue;
            }

            // 替换占位符标记
            int pos = placeholder.position;
            // 找到空字符位置并替换
            int nullCharPos = findNthNullChar(result, getNullCharCountBefore(result, pos));
            if (nullCharPos >= 0) {
                result.replace(nullCharPos, nullCharPos + 1, replacement);
            }
        }

        return result.toString();
    }

    /**
     * 辅助方法：找到第n个空字符的位置
     */
    private int findNthNullChar(StringBuilder sb, int n) {
        int count = 0;
        for (int i = 0; i < sb.length(); i++) {
            if (sb.charAt(i) == '\0') {
                if (count == n) {
                    return i;
                }
                count++;
            }
        }
        return -1;
    }

    /**
     * 辅助方法：计算指定位置前有多少个空字符
     */
    private int getNullCharCountBefore(StringBuilder sb, int pos) {
        int count = 0;
        for (int i = 0; i < pos && i < sb.length(); i++) {
            if (sb.charAt(i) == '\0') {
                count++;
            }
        }
        return count;
    }

    /**
     * 构建模型数据（扁平化实体并添加提取文本）
     */
    private Map<String, Object> buildModel(Object entity, List<String> extractTxt) {
        Map<String, Object> model = new java.util.HashMap<>();

        if (entity != null) {
            // 展开嵌套属性
            flattenObject("", entity, model);
        }

        if (extractTxt != null) {
            for (int i = 0; i < extractTxt.size(); i++) {
                model.put("value" + i, extractTxt.get(i));
            }
        }

        return model;
    }

    /**
     * 递归展开嵌套对象到扁平化的 Map 中
     * @param prefix 当前路径前缀
     * @param obj 当前对象
     * @param result 结果 Map
     */
    @SuppressWarnings("unchecked")
    private void flattenObject(String prefix, Object obj, Map<String, Object> result) {
        if (obj == null) {
            return;
        }

        if (obj instanceof Map) {
            // 处理 Map 类型
            ((Map<String, Object>) obj).forEach((key, value) -> {
                // 使用下划线代替点号作为路径分隔符
                String fullKey = prefix.isEmpty() ? key : prefix + "." + key;
                if (value instanceof Map || value instanceof List || isComplexType(value)) {
                    // 如果是复杂类型，继续展开
                    flattenObject(fullKey, value, result);
                } else {
                    // 简单类型直接添加
                    result.put(fullKey, value);
                }
            });
        } else if (obj instanceof List) {
            // 处理 List 类型
            List<Object> list = (List<Object>) obj;
            for (int i = 0; i < list.size(); i++) {
                // 使用下划线代替方括号
                String indexKey = prefix.isEmpty() ? "index_" + i : prefix + "[" + i + "]";
                Object item = list.get(i);
                if (item instanceof Map || item instanceof List || isComplexType(item)) {
                    flattenObject(indexKey, item, result);
                } else {
                    result.put(indexKey, item);
                }
            }
        } else {
            // 处理普通 Java 对象
            try {
                // 先尝试用 Jackson 转换为 Map
                Map<String, Object> converted = mapper.convertValue(obj, new TypeReference<>() {
                });
                if (converted != null && !converted.isEmpty()) {
                    flattenObject(prefix, converted, result);
                }
            } catch (IllegalArgumentException e) {
                // 如果转换失败，则直接作为值存储
                if (!prefix.isEmpty()) {
                    result.put(prefix, obj);
                }
            }
        }
    }

    /**
     * 判断是否为复杂类型（需要进一步展开）
     */
    private boolean isComplexType(Object obj) {
        if (obj == null) {
            return false;
        }
        Class<?> clazz = obj.getClass();
        // 基本类型及其包装类、String 等不需要展开
        return !(clazz.isPrimitive()
                || Number.class.isAssignableFrom(clazz)
                || Boolean.class.equals(clazz)
                || Character.class.equals(clazz)
                || String.class.equals(clazz)
                || obj instanceof Enum);
    }

    /**
         * 模板信息内部类
         */
        private record TemplateInfo(String template, List<Placeholder> placeholders) {
    }

    /**
     * 占位符内部类
     *
     * @param position       在模板中的位置
     * @param expression     表达式
     * @param defaultValue   默认值
     * @param originalLength 原始长度
     */
        private record Placeholder(int position, String expression, String defaultValue, int originalLength) {
    }
}