package com.example.demo.service.event;

import com.example.demo.pojo.event.ChatContext;

import java.util.List;

public interface ChatContextService {

    /**
     * 插入一条聊天上下文
     * @param chatContext 聊天上下文
     */
    void insert(ChatContext chatContext);

    /**
     * 获取群组聊天上下文
     * @param groupId 群组ID
     * @param botId 机器人ID
     * @return 聊天上下文列表
     */
    List<ChatContext> getGroupChatContext(Long groupId, Long botId);

    /**
     * 获取用户聊天上下文
     * @param userId 用户ID
     * @param botId 机器人ID
     * @return 聊天上下文列表
     */
    List<ChatContext> getUserChatContext(Long userId, Long botId);

    /**
     * 更新聊天上下文
     * @param chatContexts 聊天上下文列表
     * @param summaryId 上下文总结ID
     */
    void update(List<ChatContext> chatContexts, String summaryId);
}
