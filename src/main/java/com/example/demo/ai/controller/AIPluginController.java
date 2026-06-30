package com.example.demo.ai.controller;

import com.example.demo.ai.exception.CodeReviewFailedException;
import com.example.demo.ai.pojo.dto.*;
import com.example.demo.ai.service.AIPluginService;
import com.example.demo.pojo.entity.Result;
import com.example.demo.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI 插件生成 API
 */
@RestController
@RequestMapping("/ai-plugin")
@Slf4j
public class AIPluginController {

    private final AIPluginService aiPluginService;
    private final boolean reviewEnabled;

    public AIPluginController(AIPluginService aiPluginService,
                              @Value("${ai.review.enabled:false}") boolean reviewEnabled) {
        this.aiPluginService = aiPluginService;
        this.reviewEnabled = reviewEnabled;
    }

    /**
     * AI 插件生成页面配置。
     */
    @GetMapping("/config")
    public Result<Map<String, Object>> config() {
        return Result.success("", Map.of("reviewEnabled", reviewEnabled));
    }

    /**
     * 撤销到指定轮次
     */
    @PostMapping("/conversation/undo")
    public Result<GenerateResponse> undo(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody UndoRequest request) {

        try {
            GenerateResponse resp = aiPluginService.undoToRound(
                    request.getConversationId(), request.getTargetRound(), user.userId());
            return Result.success(resp);
        } catch (SecurityException e) {
            return Result.error(403, e.getMessage());
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 删除指定轮次及其之后的对话内容
     */
    @PostMapping("/conversation/delete-round")
    public Result<GenerateResponse> deleteRound(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody DeleteRoundRequest request) {

        try {
            GenerateResponse resp = aiPluginService.deleteFromRound(
                    request.getConversationId(), request.getRound(), user.userId());
            return Result.success(resp);
        } catch (SecurityException e) {
            return Result.error(403, e.getMessage());
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 加载对话历史
     */
    @GetMapping("/conversation/{conversationId}")
    public Result<ConversationResponse> loadConversation(@PathVariable("conversationId") String conversationId) {

        try {
            ConversationResponse resp = aiPluginService.loadConversation(conversationId);
            return Result.success(resp);
        } catch (IllegalArgumentException e) {
            return Result.error(404, e.getMessage());
        }
    }

    /**
     * 确认编译上传
     */
    @PostMapping("/conversation/compile")
    public Result<PluginCompileResponse> compileAndUpload(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody PluginCompileRequest request) {

        try {
            PluginCompileResponse data = aiPluginService.compileAndUpload(request, user.userId());
            return Result.success(data);
        } catch (CodeReviewFailedException e) {
            PluginCompileResponse data = PluginCompileResponse.builder()
                    .reviewResult(e.getReviewResult())
                    .build();
            return Result.error(403, e.getMessage(), data);
        } catch (IllegalStateException e) {
            return Result.error(403, e.getMessage());
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (RuntimeException e) {
            log.error("编译上传失败", e);
            return Result.error(500, e.getMessage());
        }
    }

}
