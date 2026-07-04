package com.example.demo.ai.ai.mcp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.demo.ai.ai.pojo.entity.AIChatMessage;
import com.example.demo.ai.ai.pojo.entity.Code;
import com.example.demo.ai.ai.service.AIService;
import com.example.demo.ai.ai.service.CodeService;
import com.example.demo.ai.ai.util.AIUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class PluginFileTool {

    @Value("${plugin.template.pom-path}")
    private String pomTemplatePath;

    private final AIService aiService;
    private final CodeService codeService;

    public PluginFileTool(CodeService codeService, AIService aiService) {
        this.codeService = codeService;
        this.aiService = aiService;
    }

    @Tool(description = "保存代码文件，根据路径自动判断新建还是更新")
    public Boolean saveCode(@ToolParam(description = "消息 ID（即会话ID）") String messageId,
                            @ToolParam(description = "文件路径") String path,
                            @ToolParam(description = "代码内容") String content,
                            @ToolParam(description = "文件简介") String description) {
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

    @Tool(description = "根据消息 ID 获取根据此次用户需求生成的代码与相关内容")
    public List<Code> getCode(@ToolParam(description = "消息 ID") String messageId) {
        return codeService.getBaseMapper()
                .selectList(new LambdaQueryWrapper<Code>()
                        .eq(Code::getMessageId, messageId));
    }

    @Tool(description = "根据代码 ID 删除代码与相关内容")
    public Boolean deleteCode(@ToolParam(description = "代码ID") String codeId) {
        return codeService.removeById(codeId);
    }

    @Tool(description = "获取插件pom模板")
    public String getPomTemplate() {
        try {
            return AIUtil.loadTemplate(pomTemplatePath);
        } catch (IOException e) {
            return "获取插件pom模板失败：" + e.getMessage();
        }
    }

    @Tool(description = "根据消息ID更新此次对话插件pom依赖内容")
    public Boolean updatePom(@ToolParam(description = "消息 ID") String messageId,
                             @ToolParam(description = "pom依赖内容") String pomContent) {
        LambdaUpdateWrapper<AIChatMessage> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AIChatMessage::getConversationId, messageId)
                .set(AIChatMessage::getPom, pomContent);
        return aiService.update(updateWrapper);
    }
}
