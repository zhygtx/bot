package com.generalbot.common.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 异常信息提取工具。
 */
public final class ExceptionUtils {

    private ExceptionUtils() {
    }

    /**
     * 提取可读的简要错误原因。
     * 优先取异常自带 message，为空时退化为异常类名；
     * 不再向下剥离 cause，避免丢失像“节点 xxx 执行失败：xxx”这样的上下文。
     *
     * @param error 异常
     * @return 简要错误原因
     */
    public static String brief(Throwable error) {
        if (error == null) {
            return "";
        }
        String message = error.getMessage();
        return message == null || message.isBlank() ? error.getClass().getName() : message;
    }

    /**
     * 提取完整堆栈（包含整条异常链）。
     *
     * @param error 异常
     * @return 完整堆栈字符串
     */
    public static String fullStackTrace(Throwable error) {
        if (error == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            error.printStackTrace(pw);
        }
        return sw.toString();
    }
}
