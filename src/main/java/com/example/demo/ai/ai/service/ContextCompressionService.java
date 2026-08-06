package com.example.demo.ai.ai.service;

import com.example.demo.ai.ai.pojo.entity.AIChatMessage;
import com.example.demo.ai.ai.pojo.entity.AIMessageSummary;
import com.example.demo.ai.ai.pojo.entity.Code;
import org.springframework.ai.chat.messages.Message;

import java.io.IOException;
import java.util.List;

/**
 * 上下文压缩服务：负责摘要持久化、模型目录刷新、压缩触发与 prompt 拼装。
 */
public interface ContextCompressionService {

    /** 查询会话中 summary_round 最大的摘要；无摘要时返回 null */
    AIMessageSummary findLatestSummary(String conversationId);

    /** 新增一条摘要（自动生成 id 与 createTime） */
    void insertSummary(AIMessageSummary summary);

    /**
     * 组装 Spring AI 消息列表：必要时先压缩历史，再按"最新摘要 + 尾部轮次 + 当前需求"拼接。
     *
     * @param userId          用户 ID
     * @param conversationId  会话 ID
     * @param history         当前请求之前的全部历史消息
     * @param newMessage      当前轮新消息（尚未持久化 AI 回复）
     * @param codes           当前轮代码文件
     * @param systemTemplate  写插件系统提示词
     */
    List<Message> buildMessages(String userId, String conversationId, List<AIChatMessage> history,
                                AIChatMessage newMessage, List<Code> codes, String systemTemplate) throws IOException;
}
