package com.example.demo.ai.ai.controller;

import com.example.demo.ai.ai.service.AIService;
import com.example.demo.pojo.entity.Result;
import com.example.demo.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ai-plugin")
@Slf4j
public class AIController {

    @Value("${ai.review.enabled:false}")
    private boolean reviewEnabled;

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    /**
     * 撤销指定轮次的对话记录。
     */
    @PostMapping
    public Result<?> undo(String conversationId, Integer round) {
        Integer i = aiService.undoToRound(conversationId, round);
        return i > 0 ? Result.success(i) : Result.error(400, "撤销失败");
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
        return Result.success(aiService.findDtoList(user.userId(), pageNum, pageSize));
    }

    /**
     * 根据会话 ID 获取所有消息记录。
     */
    @GetMapping("/{conversationId}")
    public Result<?> findByConversationId(@PathVariable String conversationId) {
        return Result.success(aiService.findByConversationId(conversationId));
    }
}
