package com.example.demo.service.event.impl;

import com.example.demo.mapper.event.ChatContextMapper;
import com.example.demo.pojo.event.ChatContext;
import com.example.demo.service.event.ChatContextService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatContextServiceImpl implements ChatContextService {

    private final ChatContextMapper chatContextMapper;

    public ChatContextServiceImpl(ChatContextMapper chatContextMapper) {
        this.chatContextMapper = chatContextMapper;
    }

    /**
     * 插入一条聊天上下文
     * @param chatContext 聊天上下文
     */
    @Override
    public void insert(ChatContext chatContext) {
        chatContextMapper.insert(chatContext);
    }

    /**
     * 获取最新的总结
     * @param groupId 群组ID
     * @param botId 机器人ID
     * @return 上下文
     */
    @Override
    public List<ChatContext> getGroupChatContext(Long groupId, Long botId) {
        ChatContext summary = chatContextMapper.hasGroupSummary(groupId, botId);
        return summary == null ? chatContextMapper.getByGroupId(groupId, botId) : chatContextMapper.getBySummaryId(summary.getSummaryId());
    }

    /**
     * 获取最新的总结
     * @param userId 用户ID
     * @param botId 机器人ID
     * @return 上下文
     */
    @Override
    public List<ChatContext> getUserChatContext(Long userId, Long botId) {
        ChatContext summary = chatContextMapper.hasUserSummary(userId, botId);
        return summary == null ? chatContextMapper.getByUserId(userId, botId) : chatContextMapper.getBySummaryId(summary.getSummaryId());
    }

    /**
     * 更新上下文
     * @param chatContexts 上下文
     * @param summaryId 上下文ID
     */
    @Override
    public void update(List<ChatContext> chatContexts, String summaryId) {
        chatContextMapper.update(chatContexts, summaryId);
    }
}
