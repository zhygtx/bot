package com.example.demo.ai.ai.mcp;

import com.example.demo.ai.ai.pojo.entity.Code;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 工具通知的展示文案与结果处理集中放在这里。
 * 新增工具时，只需添加一个静态工厂方法，把显示文案和结果转换 lambda 写好即可。
 * 无需再修改任何 switch 语句。
 *
 * <p>调用方式：{@code toolCallNotifier.call(messageId, ToolNotice.xxx(...), () -> doSomething(...))}</p>
 */
public class ToolNotice {

    private final String method;
    private final String displayName;
    private final String description;
    private final String category;
    private final List<Map<String, Object>> argumentsPreview;
    private final ResultHandler resultHandler;

    private ToolNotice(String method, String displayName, String description, String category,
                       List<Map<String, Object>> argumentsPreview, ResultHandler resultHandler) {
        this.method = method;
        this.displayName = displayName;
        this.description = description;
        this.category = category;
        this.argumentsPreview = argumentsPreview;
        this.resultHandler = resultHandler;
    }

    public String method() { return method; }
    public String displayName() { return displayName; }
    public String description() { return description; }
    public String category() { return category; }
    public List<Map<String, Object>> argumentsPreview() { return argumentsPreview; }
    public ResultHandler resultHandler() { return resultHandler; }

    // ================================================================
    // 统一前端通知协议
    // ================================================================

    /**
     * 无论什么工具，前端只认这三个字段：
     * <ul>
     *   <li>{@code success} — 布尔值，前端用来决定图标颜色</li>
     *   <li>{@code summary} — 字符串，前端直接展示</li>
     *   <li>{@code detail}  — 可选的额外数据，前端按需取用</li>
     * </ul>
     */
    public record ResultSummary(boolean success, String summary, Map<String, Object> detail) {

        public static ResultSummary ok(String summary) {
            return new ResultSummary(true, summary, Map.of());
        }

        public static ResultSummary ok(String summary, Map<String, Object> detail) {
            return new ResultSummary(true, summary, detail);
        }

        public static ResultSummary fail(String summary) {
            return new ResultSummary(false, summary, Map.of());
        }

        public static ResultSummary error(Throwable e) {
            return new ResultSummary(false,
                    e.getMessage() != null ? e.getMessage() : "工具调用失败",
                    Map.of("errorType", e.getClass().getSimpleName()));
        }
    }

    /**
     * 工具结果转换器：拿到工具方法的原始返回值，产出统一的 ResultSummary。
     */
    @FunctionalInterface
    public interface ResultHandler {
        ResultSummary handle(Object result);
    }

    // ================================================================
    // 工具描述（新增工具时在这里添加静态工厂方法）
    // ================================================================

    /**
     * 保存或更新代码文件。
     */
    public static ToolNotice saveCode(String path, String content, String description) {
        return new ToolNotice(
                "saveCode",
                "保存代码文件",
                "根据路径自动判断新建还是更新",
                "file",
                List.of(
                        previewText("path", "文件路径", path),
                        previewLargeText("content", "代码内容", content),
                        previewText("description", "文件简介", description)
                ),
                result -> {
                    boolean ok = Boolean.TRUE.equals(result);
                    return ok ? ResultSummary.ok("保存成功") : ResultSummary.fail("保存失败");
                }
        );
    }

    /**
     * 按代码 ID 查看单个代码文件。
     */
    public static ToolNotice getCodeById(String codeId) {
        return new ToolNotice(
                "getCodeById",
                "查看代码内容",
                "根据代码ID获取代码内容",
                "file",
                List.of(previewText("codeId", "代码ID", codeId)),
                result -> {
                    if (result == null) {
                        return ResultSummary.ok("未找到代码");
                    }
                    Code code = (Code) result;
                    return ResultSummary.ok(code.getPath() != null ? code.getPath() : "代码文件",
                            Map.of("path", code.getPath(), "lines", countLines(code.getContent())));
                }
        );
    }

    /**
     * 删除代码文件。
     */
    public static ToolNotice deleteCode(String codeId) {
        return new ToolNotice(
                "deleteCode",
                "删除代码文件",
                "根据代码ID删除代码与相关内容",
                "file",
                List.of(previewText("codeId", "代码ID", codeId)),
                result -> {
                    boolean ok = Boolean.TRUE.equals(result);
                    return ok ? ResultSummary.ok("删除成功") : ResultSummary.fail("删除失败");
                }
        );
    }

    /**
     * 读取插件 pom 模板。
     */
    public static ToolNotice getPom(String messageId) {
        return new ToolNotice(
                "getPom",
                "读取配置依赖",
                "获取插件配置依赖",
                "dependency",
                List.of(previewText("messageId", "消息ID", messageId)),
                result -> {
                    String text = (String) result;
                    if (text == null || text.isEmpty()) {
                        return ResultSummary.fail("读取 pom.xml 失败：内容为空或文件不存在");
                    }
                    return ResultSummary.ok("读取成功", Map.of(
                            "content", text,
                            "lines", countLines(text),
                            "size", text.length()
                    ));
                }
        );
    }

    /**
     * 更新当前消息的 pom 依赖内容。
     */
    public static ToolNotice updatePom(String messageId, String pomContent) {
        return new ToolNotice(
                "updatePom",
                "更新POM依赖",
                "根据消息ID更新此次对话插件POM依赖内容",
                "dependency",
                List.of(
                        previewText("messageId", "消息ID", messageId),
                        previewLargeText("pomContent", "POM内容", pomContent)
                ),
                result -> {
                    boolean ok = Boolean.TRUE.equals(result);
                    return ok ? ResultSummary.ok("更新成功") : ResultSummary.fail("更新失败");
                }
        );
    }

    /**
     * 更新插件描述。
     */
    public static ToolNotice updatePluginDescription(String messageId, String pluginName, String pluginDescription, String version, String changelog) {
        return new ToolNotice(
                "updatePluginDescription",
                "更新插件描述",
                "根据消息ID更新此次对话插件描述内容",
                "plugin",
                List.of(
                        previewText("messageId", "消息ID", messageId),
                        previewText("pluginName", "插件名称", pluginName),
                        previewText("pluginDescription", "插件简介", pluginDescription),
                        previewText("version", "版本号", version),
                        previewLargeText("changelog", "更新日志", changelog)
                ),
                result -> {
                    boolean ok = Boolean.TRUE.equals(result);
                    return ok ? ResultSummary.ok("更新成功") : ResultSummary.fail("更新失败");
                }
        );
    }

    // ================================================================
    // 参数预览辅助方法
    // ================================================================

    private static Map<String, Object> previewText(String name, String label, String value) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", name);
        map.put("label", label);
        map.put("type", value == null || value.isBlank() ? "empty" : "text");
        map.put("preview", value == null ? "空" : compactText(value));
        map.put("size", value == null ? 0 : value.length());
        return map;
    }

    private static Map<String, Object> previewLargeText(String name, String label, String value) {
        String text = value == null ? "" : value;
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", name);
        map.put("label", label);
        map.put("type", "text");
        map.put("preview", countLines(text) + " 行 / " + text.length() + " 字符");
        map.put("lines", countLines(text));
        map.put("size", text.length());
        return map;
    }

    private static String compactText(String text) {
        if (text == null || text.isBlank()) return "";
        String compact = text.replaceAll("\\s+", " ").trim();
        if (compact.length() > 120) {
            return countLines(text) + " 行 / " + text.length() + " 字符";
        }
        return compact;
    }

    private static int countLines(String content) {
        if (content == null || content.isEmpty()) return 0;
        return content.split("\\R", -1).length;
    }
}
