package com.example.demo.ai.service;

import com.example.demo.ai.pojo.dto.*;

/**
 * AI 插件服务接口。<br>
 * 纯业务逻辑，不处理 HTTP 层关注点（校验、鉴权、Result 包装由 Controller 负责）。
 */
public interface AIPluginService {

    // ==================== AI 代码生成 ====================

    /** 新建/微调对话轮次，流式返回模型增量 */
    GenerateResponse createStreamTurn(GenerateRequest startRequest,
                                      TurnRequest turnRequest,
                                      String userId,
                                      java.util.function.Consumer<String> onDelta);

    /** 撤销到指定轮次 */
    GenerateResponse undoToRound(String conversationId, int targetRound, String userId);

    /** 删除指定轮次及之后的内容，并恢复上一轮代码 */
    GenerateResponse deleteFromRound(String conversationId, int round, String userId);

    /** 加载对话历史 */
    ConversationResponse loadConversation(String conversationId);

    // ==================== 编译上传 ====================

    /** 审查、编译源码并上传为插件 */
    PluginCompileResponse compileAndUpload(PluginCompileRequest request, String userId);
}
