package com.example.demo.ai.ai.mcp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.demo.ai.ai.pojo.entity.AIChatMessage;
import com.example.demo.ai.ai.pojo.entity.Code;
import com.example.demo.ai.ai.service.AIService;
import com.example.demo.ai.ai.service.CodeService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PluginFileTool {

    private final AIService aiService;
    private final CodeService codeService;
    private final ToolCallNotifier toolCallNotifier;

    public PluginFileTool(CodeService codeService, AIService aiService, ToolCallNotifier toolCallNotifier) {
        this.codeService = codeService;
        this.aiService = aiService;
        this.toolCallNotifier = toolCallNotifier;
    }

    @Tool(description = "保存代码文件，根据路径自动判断新建还是更新")
    public Boolean saveCode(@ToolParam(description = "消息 ID（即会话ID）") String messageId,
                            @ToolParam(description = "文件路径") String path,
                            @ToolParam(description = "代码内容") String content,
                            @ToolParam(description = "文件简介") String description) {
        return toolCallNotifier.call(messageId, ToolNotice.saveCode(path, content, description),
                () -> doSaveCode(messageId, path, content, description));
    }

    @Tool(description = "根据代码ID获取代码内容")
    public Code getCodeById(@ToolParam(description = "代码ID") String codeId) {
        return toolCallNotifier.call(null, ToolNotice.getCodeById(codeId),
                () -> codeService.getById(codeId));
    }

    @Tool(description = "根据代码 ID 删除代码与相关内容")
    public Boolean deleteCode(@ToolParam(description = "代码ID") String codeId) {
        return toolCallNotifier.call(null, ToolNotice.deleteCode(codeId),
                () -> codeService.removeById(codeId));
    }

    @Tool(description = "根据消息 ID 获取插件pom模板")
    public String getPomTemplate(@ToolParam(description = "消息 ID") String messageId) {
        return toolCallNotifier.call(null, ToolNotice.getPom(messageId),
                () -> aiService.getById(messageId).getPom());
    }

    @Tool(description = "根据消息ID更新此次对话插件pom依赖内容")
    public Boolean updatePom(@ToolParam(description = "消息 ID") String messageId,
                             @ToolParam(description = "pom依赖内容") String pomContent) {
        return toolCallNotifier.call(messageId, ToolNotice.updatePom(messageId, pomContent),
                () -> doUpdatePom(messageId, pomContent));
    }

    @Tool(description = "根据消息 ID 更新此次对话插件描述内容")
    public Boolean updatePluginDescription(@ToolParam(description = "消息 ID") String messageId,
                                           @ToolParam(description = "插件名称") String pluginName,
                                           @ToolParam(description = "插件描述") String pluginDescription,
                                           @ToolParam(description = "插件版本") String version,
                                           @ToolParam(description = "插件更新日志") String changelog) {
        return toolCallNotifier.call(messageId, ToolNotice.updatePluginDescription(messageId, pluginName, pluginDescription, version, changelog),
                () -> doUpdatePluginDescription(messageId, pluginName, pluginDescription, version, changelog));
    }

    /**
     * 根据消息 ID 获取根据此次用户需求生成的代码与相关内容
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
