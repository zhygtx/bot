package com.generalbot.theme.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 用户自定义 CSS 校验器测试。
 * 覆盖合法 CSS、危险规则、注释/字符串、转义绕过、括号完整性和 Markdown 围栏。
 */
class ThemeCssValidatorTest {

    @Test
    void emptyCssReturnsEmpty() {
        assertEquals("", ThemeCssValidator.validate(null));
        assertEquals("", ThemeCssValidator.validate("  \n "));
    }

    @Test
    void modernCssIsAccepted() {
        String css = """
                :root {
                  --custom-radius: 14px;
                }
                @layer components {
                  @media (max-width: 600px) {
                    .el-card { border-radius: var(--custom-radius); }
                  }
                }
                @keyframes fade-in {
                  from { opacity: 0; }
                  to { opacity: 1; }
                }
                """;
        assertEquals(css.trim(), ThemeCssValidator.validate(css));
    }

    @Test
    void urlInsideCommentOrStringIsIgnored() {
        String css = """
                /* url(https://example.com) 只是注释 */
                .tip::after { content: "url(fake)"; }
                """;
        assertEquals(css.trim(), ThemeCssValidator.validate(css));
    }

    @Test
    void importRuleIsRejected() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> ThemeCssValidator.validate("@import url('https://example.com/x.css');")
        );
        assertTrue(error.getMessage().contains("@import"));
    }

    @Test
    void urlFunctionIsRejected() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> ThemeCssValidator.validate(".bg { background: url(https://example.com/a.png); }")
        );
        assertTrue(error.getMessage().contains("url("));
    }

    @Test
    void escapedImportIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ThemeCssValidator.validate("@imp\\6frt 'x.css';")
        );
    }

    @Test
    void unbalancedDelimiterIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ThemeCssValidator.validate(".a { color: red;")
        );
    }

    @Test
    void oversizedCssIsRejected() {
        String css = "x".repeat(64 * 1024 + 1);
        assertThrows(IllegalArgumentException.class, () -> ThemeCssValidator.validate(css));
    }

    @Test
    void markdownCodeFenceIsRejected() {
        String css = """
                ```css
                :root { --app-primary: #1677ff; }
                ```
                """;
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> ThemeCssValidator.validate(css)
        );
        assertTrue(error.getMessage().contains("反引号"));
    }
}
