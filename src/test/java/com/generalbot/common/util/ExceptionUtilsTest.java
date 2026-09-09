package com.generalbot.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 异常信息提取工具测试，重点覆盖“只看到类名看不到原因”的回归场景。
 */
class ExceptionUtilsTest {

    @Test
    void briefHandlesNull() {
        assertEquals("", ExceptionUtils.brief(null));
    }

    @Test
    void briefKeepsWrapperContextInsteadOfPeelingToCause() {
        NoClassDefFoundError cause = new NoClassDefFoundError("com/mikuac/shiro/common/utils/MsgUtils");
        RuntimeException wrapper = new RuntimeException("节点 发消息 执行失败：" + cause.getMessage(), cause);

        String brief = ExceptionUtils.brief(wrapper);

        assertEquals("节点 发消息 执行失败：com/mikuac/shiro/common/utils/MsgUtils", brief);
    }

    @Test
    void briefFallsBackToClassNameWhenBlank() {
        assertEquals("java.lang.RuntimeException", ExceptionUtils.brief(new RuntimeException(" ")));
    }

    @Test
    void fullStackTraceContainsWholeCauseChain() {
        NoClassDefFoundError cause = new NoClassDefFoundError("com/mikuac/shiro/common/utils/MsgUtils");
        RuntimeException wrapper = new RuntimeException("节点 发消息 执行失败：" + cause.getMessage(), cause);

        String stack = ExceptionUtils.fullStackTrace(wrapper);

        assertTrue(stack.contains("节点 发消息 执行失败：com/mikuac/shiro/common/utils/MsgUtils"));
        assertTrue(stack.contains("Caused by: java.lang.NoClassDefFoundError"));
        assertTrue(stack.contains("com/mikuac/shiro/common/utils/MsgUtils"));
    }
}
