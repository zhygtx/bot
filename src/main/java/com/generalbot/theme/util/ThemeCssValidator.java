package com.generalbot.theme.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用户自定义 CSS 校验器。
 * 不走 token 白名单，改为语法结构校验 + 危险规则黑名单，允许 @media/@keyframes/@layer 等现代 CSS。
 */
public final class ThemeCssValidator {

    /**
     * 自定义 CSS 最大长度，超过后拒绝保存。
     */
    private static final int MAX_CSS_LENGTH = 64 * 1024;

    /**
     * 危险规则黑名单。
     * url()、@import、expression 等会引入外部资源或旧浏览器执行能力，当前阶段统一禁止。
     */
    private static final Pattern FORBIDDEN_CSS_PATTERN = Pattern.compile(
            "(?i)(@import|@charset|@namespace|@(?:-moz-)?document|@font-face|url\\s*\\(|expression\\s*\\(|javascript:|behavior\\s*:|\\-moz-binding|progid\\s*:)"
    );

    private ThemeCssValidator() {
    }

    /**
     * 校验并规范化用户自定义 CSS。
     * 返回去除首尾空白后的原文；空字符串表示清空自定义样式。
     */
    public static String validate(String css) {
        String normalized = css == null ? "" : css.trim();
        if (normalized.isEmpty()) {
            return "";
        }
        if (normalized.length() > MAX_CSS_LENGTH) {
            throw new IllegalArgumentException("自定义 CSS 不能超过 64KB");
        }
        validateControlCharacters(normalized);

        String scanned = stripCommentsAndStrings(normalized);
        validateBalancedDelimiters(scanned);
        Matcher matcher = FORBIDDEN_CSS_PATTERN.matcher(scanned);
        if (matcher.find()) {
            throw new IllegalArgumentException("自定义 CSS 包含不允许的内容：" + matcher.group());
        }
        return normalized;
    }

    /**
     * 拒绝换行制表符之外的控制字符，避免把不可见内容写进数据库。
     */
    private static void validateControlCharacters(String css) {
        for (int i = 0; i < css.length(); i++) {
            char c = css.charAt(i);
            if (c < 0x20 && c != '\n' && c != '\r' && c != '\t') {
                throw new IllegalArgumentException("自定义 CSS 包含非法控制字符");
            }
        }
    }

    /**
     * 去掉注释和字符串后再检查结构，保证 url() 写在字符串或注释里不会误伤。
     * 同时把 CSS 转义归一化，防止 @imp\\6frt 这类写法绕过黑名单。
     */
    private static String stripCommentsAndStrings(String css) {
        StringBuilder filtered = new StringBuilder(css.length());
        int index = 0;
        while (index < css.length()) {
            char current = css.charAt(index);
            if (current == '/' && index + 1 < css.length() && css.charAt(index + 1) == '*') {
                int end = css.indexOf("*/", index + 2);
                if (end < 0) {
                    throw new IllegalArgumentException("自定义 CSS 注释未闭合");
                }
                index = end + 2;
                continue;
            }
            if (current == '\'' || current == '"') {
                int end = findStringEnd(css, index);
                if (end < 0) {
                    throw new IllegalArgumentException("自定义 CSS 字符串未闭合");
                }
                index = end + 1;
                continue;
            }
            if (current == '\\') {
                index = appendNormalizedEscape(css, index, filtered);
                continue;
            }
            filtered.append(current);
            index++;
        }
        return filtered.toString();
    }

    /**
     * 查找字符串结束位置，字符串内的反斜杠转义按两个字符跳过。
     */
    private static int findStringEnd(String css, int start) {
        char quote = css.charAt(start);
        int index = start + 1;
        while (index < css.length()) {
            char current = css.charAt(index);
            if (current == '\\') {
                index += 2;
                continue;
            }
            if (current == quote) {
                return index;
            }
            index++;
        }
        return -1;
    }

    /**
     * 把 CSS 转义归一化后写入扫描文本，返回下一次读取位置。
     */
    private static int appendNormalizedEscape(String css, int index, StringBuilder filtered) {
        if (index + 1 >= css.length()) {
            throw new IllegalArgumentException("自定义 CSS 转义不完整");
        }
        int next = index + 1;
        char escaped = css.charAt(next);
        if (isHexDigit(escaped)) {
            int end = next + 1;
            int codePoint = hexValue(escaped);
            while (end < css.length() && end - next < 6 && isHexDigit(css.charAt(end))) {
                codePoint = codePoint * 16 + hexValue(css.charAt(end));
                end++;
            }
            if (end < css.length() && (css.charAt(end) == ' ' || css.charAt(end) == '\t' || css.charAt(end) == '\n' || css.charAt(end) == '\r')) {
                end++;
            }
            filtered.appendCodePoint(Math.min(codePoint, 0x10FFFF));
            return end;
        }
        filtered.append(escaped);
        return next + 1;
    }

    /**
     * 判断字符是否为 CSS 十六进制转义字符。
     */
    private static boolean isHexDigit(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    /**
     * 把十六进制字符转成数值。
     */
    private static int hexValue(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        }
        return c - 'A' + 10;
    }

    /**
     * 校验大括号、圆括号、方括号成对闭合。
     */
    private static void validateBalancedDelimiters(String css) {
        Deque<Character> stack = new ArrayDeque<>();
        for (int i = 0; i < css.length(); i++) {
            char current = css.charAt(i);
            switch (current) {
                case '{' -> stack.push('}');
                case '(' -> stack.push(')');
                case '[' -> stack.push(']');
                case '}', ')', ']' -> {
                    if (stack.isEmpty() || stack.pop() != current) {
                        throw new IllegalArgumentException("自定义 CSS 括号未闭合或顺序错误");
                    }
                }
                default -> {
                }
            }
        }
        if (!stack.isEmpty()) {
            throw new IllegalArgumentException("自定义 CSS 括号未闭合");
        }
    }
}
