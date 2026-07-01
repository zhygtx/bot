package com.example.demo.ai.controller;

import com.example.demo.ai.exception.CodeReviewFailedException;
import com.example.demo.ai.pojo.dto.PluginCompileRequest;
import com.example.demo.ai.pojo.dto.PluginCompileResponse;
import com.example.demo.ai.service.AIPluginService;
import com.example.demo.pojo.entity.Result;
import com.example.demo.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 插件生成 API
 */
@RestController
@RequestMapping("/ai-plugin")
@Slf4j
public class AIPluginController {

    private final AIPluginService aiPluginService;

    @Value("${ai.review.enabled:false}")
    private boolean reviewEnabled;

    public AIPluginController(AIPluginService aiPluginService) {
        this.aiPluginService = aiPluginService;
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
