package com.example.demo.ai.ai.tool;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.demo.ai.ai.pojo.entity.AIChatMessage;
import com.example.demo.ai.ai.pojo.entity.Code;
import com.example.demo.ai.ai.service.AIService;
import com.example.demo.ai.ai.service.CodeService;
import com.example.demo.ai.ai.util.SseStream;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * AI 编写插件时调用的文件操作工具集。
 *
 * <p>每个 {@code @Tool} 方法通过 {@link #executeTool} 骨架统一推送三态事件：
 * <ul>
 *   <li>开始：{@code toolCallStart(name)} 推送 {@code RUNNING}</li>
 *   <li>成功：{@code toolCallFinish(part, name, "SUCCESS")}</li>
 *   <li>失败：{@code toolCallFinish(part, name, "ERROR")}</li>
 * </ul>
 * &#064;Tool  方法本身只关心业务逻辑，不直接处理 SSE 或状态机。
 */
@Component
public class PluginFileTool {

    private final AIService aiService;
    private final CodeService codeService;

    public PluginFileTool(CodeService codeService, AIService aiService) {
        this.codeService = codeService;
        this.aiService = aiService;
    }

    @Tool(description = "保存代码文件，根据路径自动判断新建还是更新")
    public Boolean saveCode(ToolContext toolContext,
                            @ToolParam(description = "消息 ID（即会话ID）") String messageId,
                            @ToolParam(description = "文件路径") String path,
                            @ToolParam(description = "代码内容") String content,
                            @ToolParam(description = "文件简介") String description) {
        return executeTool(toolContext, "保存代码文件", () -> doSaveCode(messageId, path, content, description));
    }

    @Tool(description = "根据代码ID获取代码内容")
    public Code getCodeById(ToolContext toolContext,
                            @ToolParam(description = "消息 ID（即会话ID）") String messageId,
                            @ToolParam(description = "代码ID") String codeId) {
        return executeTool(toolContext, "查看代码内容", () -> codeService.getById(codeId));
    }

    @Tool(description = "根据代码 ID 删除代码与相关内容")
    public Boolean deleteCode(ToolContext toolContext,
                              @ToolParam(description = "消息 ID（即会话ID）") String messageId,
                              @ToolParam(description = "代码ID") String codeId) {
        return executeTool(toolContext, "删除代码文件", () -> codeService.removeById(codeId));
    }

    @Tool(description = "根据消息 ID 获取插件pom模板")
    public String getPomTemplate(ToolContext toolContext,
                                 @ToolParam(description = "消息 ID") String messageId) {
        return executeTool(toolContext, "读取配置依赖", () -> aiService.getById(messageId).getPom());
    }

    @Tool(description = "根据消息ID更新此次对话插件pom依赖内容")
    public Boolean updatePom(ToolContext toolContext,
                             @ToolParam(description = "消息 ID") String messageId,
                             @ToolParam(description = "pom依赖内容") String pomContent) {
        return executeTool(toolContext, "更新POM依赖", () -> doUpdatePom(messageId, pomContent));
    }

    @Tool(description = "根据消息 ID 更新此次对话插件描述内容")
    public Boolean updatePluginDescription(ToolContext toolContext,
                                           @ToolParam(description = "消息 ID") String messageId,
                                           @ToolParam(description = "插件名称") String pluginName,
                                           @ToolParam(description = "插件描述") String pluginDescription,
                                           @ToolParam(description = "插件版本") String version,
                                           @ToolParam(description = "插件更新日志") String changelog) {
        return executeTool(toolContext, "更新插件描述",
                () -> doUpdatePluginDescription(messageId, pluginName, pluginDescription, version, changelog));
    }

    /**
     * 工具调用通用骨架：toolCallStart 推送 RUNNING → 执行业务 → toolCallFinish 推送 SUCCESS/ERROR。
     * parts 收集和 SSE 推送全部由 SseStream 统一处理。
     */
    private <T> T executeTool(ToolContext toolContext, String name, Supplier<T> action) {
        SseStream stream = (SseStream) toolContext.getContext().get("stream");
        Map<String, Object> part = stream.toolCallStart(name);
        try {
            T result = action.get();
            stream.toolCallFinish(part, name, "SUCCESS");
            return result;
        } catch (RuntimeException | Error e) {
            stream.toolCallFinish(part, name, "ERROR");
            throw e;
        }
    }

    /**
     * 根据消息 ID 保存代码：存在则更新，不存在则新建
     */
    private Boolean doSaveCode(String messageId, String path, String content, String description) {
        Code existing = codeService.getOne(new LambdaQueryWrapper<Code>()
                .eq(Code::getMessageId, messageId)
                .eq(Code::getPath, path));
        if (existing != null) {
            existing.setContent(content);
            existing.setDescription(description);
            return codeService.updateById(existing);
        }
        Code code = new Code();
        code.setId(UUID.randomUUID().toString());
        code.setMessageId(messageId);
        code.setPath(path);
        code.setContent(content);
        code.setDescription(description);
        return codeService.save(code);
    }

    /**
     * 根据消息ID更新此次对话插件pom依赖内容
     */
    private Boolean doUpdatePom(String messageId, String pomContent) {
        LambdaUpdateWrapper<AIChatMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AIChatMessage::getId, messageId)
                .set(AIChatMessage::getPom, pomContent);
        return aiService.update(updateWrapper);
    }

    /**
     * 根据消息 ID 更新此次对话插件描述内容
     */
    private Boolean doUpdatePluginDescription(String messageId, String pluginName, String pluginDescription, String version, String changelog) {
        LambdaUpdateWrapper<AIChatMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AIChatMessage::getId, messageId)
                .set(AIChatMessage::getPluginName, pluginName)
                .set(AIChatMessage::getPluginDescription, pluginDescription)
                .set(AIChatMessage::getVersion, version)
                .set(AIChatMessage::getChangelog, changelog);
        return aiService.update(updateWrapper);
    }
}
