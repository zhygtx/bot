package com.example.demo.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * StringTemplate 渲染服务
 * - 支持用户写 {{...}} 语法
 * - 支持 {{expr|默认}} -> 如果 expr 存在则输出 expr 否则输出 默认
 * - 未命中且无默认值时输出字符串 "null"
 */
@Component
public class StringTemplateUtil {

    private final ObjectMapper mapper;

    // 缓存预处理后的模板字符串，key 可以是原模板文本的 hash 或原模板本身
    private final ConcurrentHashMap<String, String> templateCache = new ConcurrentHashMap<>();

    public StringTemplateUtil(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 渲染模板
     * @param tpl 原始模板，使用 {{...}} 语法
     * @param entity 单个实体对象（POJO/Map/JsonNode），可为 null
     * @param extractTxt 可选提取文本列表（用于 valueN），可为 null
     * @return 渲染结果
     */
    public String render(String tpl, Object entity, List<String> extractTxt) {
        if (tpl == null || !tpl.contains("{{")) {
            return tpl;
        }

        // 1. 预处理并缓存
        String processed = templateCache.computeIfAbsent(tpl, this::preprocess);

        // 2. 构建属性 map（把实体展开到根上下文）
        Map<String, Object> model = buildModel(entity, extractTxt);

        // 3. 创建 ST 实例并渲染
        // ST 默认分隔符是 '$'，我们在预处理阶段把 {{...}} 转为 $...$ 形式
        ST st = new ST(processed, '$', '$');
        // 将 model 中的键值设置为 ST 属性
        model.forEach(st::add);

        // 渲染并返回
        return st.render();
    }

    /**
     * 把实体转换为 Map 并把 valueN 映射加入
     */
    private Map<String, Object> buildModel(Object entity, List<String> extractTxt) {
        Map<String, Object> model = new java.util.HashMap<>();
        if (entity != null) {
            // 使用 TypeReference 修复泛型警告并保证类型安全
            Map<String, Object> converted = mapper.convertValue(entity, new TypeReference<>() {
            });
            if (converted != null) {
                model.putAll(converted);
            }
        }
        if (extractTxt != null) {
            for (int i = 0; i < extractTxt.size(); i++) {
                model.put("value" + (i + 1), extractTxt.get(i));
            }
        }
        // 注意：未提供的 valueN 不放入 model，预处理会保证输出 defaultNull
        return model;
    }

    /**
     * 预处理模板
     * - 把 {{expr|默认}} -> $if(expr)$...$else$默认$endif$
     * - 把 {{expr}} -> $if(expr)$${expr}$else$null$endif$
     * - 把 {{valueN}} -> $if(valueN)$${valueN}$else$null$endif$
     * 说明：StringTemplate 的条件语法为 $if(expr)$ ... $else$ ... $endif$
     *       这里用 expr 的存在性/真值来判断是否命中
     */
    private String preprocess(String tpl) {
        // 简单状态机或正则替换实现
        // 使用正则匹配 {{...}}，非贪婪
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\{\\{\\s*(.+?)\\s*}}");
        java.util.regex.Matcher m = p.matcher(tpl);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String inner = m.group(1).trim();
            String replacement;
            // 支持默认值管道 expr|默认
            if (inner.contains("|")) {
                String[] parts = inner.split("\\|", 2);
                String expr = parts[0].trim();
                String def = escapeDollarAndBackslash(parts[1].trim());
                // $if(expr)$${expr}$else$def$endif$
                replacement = "$if(" + expr + ")$" + "${" + expr + "}$" + "$else$" + def + "$endif$";
            } else {
                // 普通表达式或 valueN
                // 当 valueN 未提供时的默认字符串
                String def = "null";
                // $if(expr)$${expr}$else$null$endif$
                replacement = "$if(" + inner + ")$" + "${" + inner + "}$" + "$else$" + def + "$endif$";
            }
            m.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * 转义 $ 和 \，以免干扰 ST 语法（当默认值包含 $ 时）
     */
    private String escapeDollarAndBackslash(String s) {
        if (s == null) return null;
        return s.replace("\\", "\\\\").replace("$", "\\$");
    }
}