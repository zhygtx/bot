package com.example.demo.ai.ai.mcp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.demo.ai.ai.pojo.entity.AIChatMessage;
import com.example.demo.ai.ai.pojo.entity.Code;
import com.example.demo.ai.ai.service.AIService;
import com.example.demo.ai.ai.service.CodeService;
import com.example.demo.ai.ai.util.AIUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class PluginFileTool {

    // ==================== 工具元数据缓存 ====================

    /**
     * 工具元数据：方法名 → (描述, 参数名→参数描述)
     */
    public record ToolMeta(String description, Map<String, String> params) {}

    /** 静态缓存，类加载时通过反射扫描 @Tool 注解自动构建 */
    public static final Map<String, ToolMeta> TOOL_META_CACHE;

    static {
        TOOL_META_CACHE = new LinkedHashMap<>();
        for (Method method : PluginFileTool.class.getDeclaredMethods()) {
            Tool tool = method.getAnnotation(Tool.class);
            if (tool == null) continue;
            Map<String, String> params = new LinkedHashMap<>();
            for (Parameter param : method.getParameters()) {
                ToolParam tp = param.getAnnotation(ToolParam.class);
                if (tp != null) {
                    params.put(param.getName(), tp.description());
                }
            }
            TOOL_META_CACHE.put(method.getName(), new ToolMeta(tool.description(), params));
        }
    }

    /**
     * 将 ToolCall 转为前端 tool_call 事件的数据。
     * 所有工具调用统一走这个方法，以后无需在调用方写 switch。
     */
    public static Map<String, Object> buildEventData(AssistantMessage.ToolCall tc, ObjectMapper mapper) {
        ToolMeta meta = TOOL_META_CACHE.get(tc.name());
        JsonNode args;
        try {
            args = mapper.readTree(tc.arguments());
        } catch (IOException e) {
            throw new RuntimeException("解析工具参数失败: " + tc.name(), e);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("method", tc.name());
        data.put("description", meta != null ? meta.description() : tc.name());
        data.put("params", meta != null ? meta.params() : Map.of());
        data.put("arguments", mapper.convertValue(args, Map.class));
        return data;
    }

    // ==================== 实例字段 ====================

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

    @Tool(description = "根据代码ID获取代码内容")
    public Code getCodeById(@ToolParam(description = "代码ID") String codeId) {
        return codeService.getById(codeId);
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
