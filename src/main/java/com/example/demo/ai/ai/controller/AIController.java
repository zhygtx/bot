package com.example.demo.ai.ai.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
        Boolean undo = aiService.undo(conversationId, round);
        return undo ? Result.success(null,null) : Result.error(400, "撤销失败", null);
    }

    @PutMapping
    public Result<?> update(@AuthenticationPrincipal UserPrincipal user, @RequestBody AIChatMessage aiChatMessage) {
        LambdaUpdateWrapper<AIChatMessage> updateWrapper = new LambdaUpdateWrapper<AIChatMessage>()
                .eq(AIChatMessage::getId, aiChatMessage.getId())
                .eq(AIChatMessage::getUserId, user.userId())
                .set(AIChatMessage::getPluginName, aiChatMessage.getPluginName())
                .set(AIChatMessage::getPluginDescription, aiChatMessage.getPluginDescription())
                .set(AIChatMessage::getVersion, aiChatMessage.getVersion())
                .set(AIChatMessage::getChangelog, aiChatMessage.getChangelog())
                .set(AIChatMessage::getIsPublic, aiChatMessage.getIsPublic());
        boolean update = aiService.update(null, updateWrapper);
        return update ? Result.success(null,null) : Result.error(400, "更新失败", null);
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
        return Result.success(null, aiService.findByConversationId(conversationId));
    }

    // ────────── SSE 端点 ──────────

    /**
     * SSE 流式代码生成。
     * <p>
     * Controller 只负责创建 emitter、调度异步任务、异常兜底。
     * 所有 SSE 事件（delta / done / error）由 AIService 通过 SseStream 统一推送。
     * </p>
     * 事件类型：
     * <ul>
     *   <li>{@code delta} — 统一增量事件，data.type 区分 thinking / text / tool_call：
     *     <ul>
     *       <li>thinking: { type:"thinking", content:"..." }</li>
     *       <li>text: { type:"text", content:"..." }</li>
     *       <li>tool_call: { type:"tool_call", name:"...", status:"RUNNING"|"SUCCESS"|"ERROR" }</li>
     *     </ul>
     *   </li>
     *   <li>{@code done} — 生成完成，data 为完整消息对象</li>
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
                aiService.aiGenerate(message, conversationId, user.userId(), emitter);
                emitter.complete();
            } catch (Exception e) {
                log.error("AI 生成任务执行异常", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * SSE 流式编译上传。
     * <p>
     * Controller 只负责创建 emitter、调度异步任务、异常兜底。
     * 所有 SSE 事件由 AIService.compileCode 通过 SseStream 统一推送。
     * </p>
     */
    @PostMapping(value = "/compile", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter compile(@RequestParam(required = false) String conversationId) {
        SseEmitter emitter = new SseEmitter(sseTimeout);

        generationExecutor.execute(() -> {
            try {
                aiService.compileCode(conversationId, emitter);
                emitter.complete();
            } catch (Exception e) {
                log.error("编译任务执行异常", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}
