package com.example.demo.ai.service;

import com.example.demo.ai.pojo.dto.*;
import com.example.demo.ai.pojo.entity.AIConversationTurn;

import java.util.List;
import java.util.Map;

/**
 * AI 插件服务接口。<br>
 * 纯业务逻辑，不处理 HTTP 层关注点（校验、鉴权、Result 包装由 Controller 负责）。
 */
public interface AIPluginService {

    // ==================== AI 代码生成 ====================

    /** 新建对话 / 首次生成代码 */
    GenerateResponse startConversation(GenerateRequest request, String userId);

    /** 微调对话轮次 */
    GenerateResponse createTurn(TurnRequest request, String userId);

    /** 撤销到指定轮次 */
    GenerateResponse undoToRound(String conversationId, int targetRound, String userId);

    /** 加载对话历史 */
    ConversationResponse loadConversation(String conversationId);

    // ==================== 编译上传 ====================

    /** 编译源码并上传为插件，返回 {pluginId, versionId} */
    Map<String, String> compileAndUpload(PluginCompileRequest request, String userId);
}
