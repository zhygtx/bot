package com.example.demo.ai.controller;

import com.example.demo.ai.pojo.dto.*;
import com.example.demo.ai.service.AIPluginService;
import com.example.demo.pojo.entity.Result;
import com.example.demo.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
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

    public AIPluginController(AIPluginService aiPluginService) {
        this.aiPluginService = aiPluginService;
    }

    /**
     * 新建对话 / 首次生成代码
     */
    @PostMapping("/conversation/start")
    public Result<GenerateResponse> startConversation(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody GenerateRequest request) {

        try {
            GenerateResponse resp = aiPluginService.startConversation(request, user.userId());
            return Result.success(resp);
        } catch (RuntimeException e) {
            log.error("AI 生成失败", e);
            return Result.error(500, e.getMessage());
        }
    }

    /**
     * 微调对话轮次
     */
    @PostMapping("/conversation/turn")
    public Result<GenerateResponse> createTurn(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody TurnRequest request) {

        try {
            GenerateResponse resp = aiPluginService.createTurn(request, user.userId());
            return Result.success(resp);
        } catch (SecurityException e) {
            return Result.error(403, e.getMessage());
        } catch (RuntimeException e) {
            log.error("AI 微调失败", e);
            return Result.error(500, e.getMessage());
        }
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
    public Result<Map<String, String>> compileAndUpload(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody PluginCompileRequest request) {

        try {
            Map<String, String> data = aiPluginService.compileAndUpload(request, user.userId());
            return Result.success(data);
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
