package com.example.demo.ai.ai.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.ai.ai.pojo.dto.CompileCodeDto;
import com.example.demo.ai.ai.pojo.entity.AIChatMessage;
import com.example.demo.ai.ai.service.AIService;
import com.example.demo.pojo.entity.Result;
import com.example.demo.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/ai-plugin")
@Slf4j
public class AIController {

    @Value("${ai.review.enabled:false}")
    private boolean reviewEnabled;

    @Value("${ai.sse.timeout-ms:300000}")
    private long sseTimeout;

    private final AIService aiService;
    private final Executor generationExecutor = Executors.newCachedThreadPool();

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    /**
     * 撤销指定轮次的对话记录。
     */
    @PostMapping
    public Result<?> undo(String conversationId, Integer round) {
        boolean remove = aiService.remove(Wrappers.lambdaQuery(AIChatMessage.class)
                .eq(AIChatMessage::getConversationId, conversationId)
                .eq(AIChatMessage::getRound, round));
        return remove ? Result.success(null) : Result.error(400, "撤销失败");
    }

    /**
     * AI 插件生成页面配置。
     */
    @GetMapping("/config")
    public Result<Map<String, Object>> config() {
        return Result.success("", Map.of("reviewEnabled", reviewEnabled));
    }

    /**
     * 根据用户 ID 查询所有消息记录。
     */
    @GetMapping("list")
    public Result<?> findDtoList(@AuthenticationPrincipal UserPrincipal user,
                                 @RequestParam(required = false,defaultValue = "1") Integer pageNum,
                                 @RequestParam(required = false,defaultValue = "10") Integer pageSize) {
        return Result.success(null, aiService.findDtoList(user.userId(), pageNum, pageSize));
    }

    /**
     * 根据会话 ID 获取所有消息记录。
     */
    @GetMapping("/{conversationId}")
    public Result<?> findByConversationId(@PathVariable String conversationId) {
        List<AIChatMessage> aiChatMessages = aiService.lambdaQuery()
                .eq(AIChatMessage::getConversationId, conversationId)
                .orderByAsc(AIChatMessage::getRound)
                .list();
        return Result.success(null, aiChatMessages);
    }

    // ────────── SSE 端点 ──────────

    /**
     * SSE 流式代码生成。
     * <p>
     * 事件类型：
     * <ul>
     *   <li>{@code delta} — 增量文本（打字机效果）</li>
     *   <li>{@code tool_call} — 工具调用事件</li>
     *   <li>{@code done} — 生成完成，data 为完整消息列表</li>
     *   <li>{@code error} — 出错，data 含 message 字段</li>
     * </ul>
     */
    @PostMapping(value = "/generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generate(@RequestParam String message,
                               @RequestParam(required = false) String conversationId,
                               @AuthenticationPrincipal UserPrincipal user) {
        SseEmitter emitter = new SseEmitter(sseTimeout);

        generationExecutor.execute(() -> {
            try {
                List<AIChatMessage> messages = aiService.aiGenerate(
                        message, conversationId, user.userId(),
                        (type, data) -> sendSseEvent(emitter, type, data), null);

                sendSseEvent(emitter, "done", messages);
                emitter.complete();
            } catch (Exception e) {
                log.error("AI 生成任务执行异常", e);
                try {
                    sendSseEvent(emitter, "error", Map.of("message", e.getMessage()));
                } catch (Exception ignored) {}
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * SSE 流式编译上传。
     */
    @PostMapping(value = "/compile", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter compile(@RequestBody CompileCodeDto compileCodeDto) {
        SseEmitter emitter = new SseEmitter(sseTimeout);

        generationExecutor.execute(() -> {
            try {
                String pluginId = aiService.compileCode(compileCodeDto,
                        (type, data) -> sendSseEvent(emitter, type, data));

                sendSseEvent(emitter, "done", pluginId);
                emitter.complete();
            } catch (Exception e) {
                log.error("编译任务执行异常", e);
                try {
                    sendSseEvent(emitter, "error", Map.of("message", e.getMessage()));
                } catch (Exception ignored) {}
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    // ────────── 私有工具方法 ──────────

    private void sendSseEvent(SseEmitter emitter, String eventName, Object data) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(data == null ? Map.of() : data));
        } catch (IOException e) {
            throw new RuntimeException("SSE 发送失败", e);
        }
    }
}
