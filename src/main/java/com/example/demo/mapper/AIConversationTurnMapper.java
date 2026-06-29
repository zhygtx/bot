package com.example.demo.mapper;

import com.example.demo.ai.pojo.entity.AIConversationTurn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI 对话轮次 Mapper
 */
@Mapper
public interface AIConversationTurnMapper {

    /**
     * 插入一条对话记录
     */
    int insert(AIConversationTurn turn);

    /**
     * 根据会话 ID 查询所有记录，按轮次升序排列
     */
    List<AIConversationTurn> selectByConversationId(String conversationId);

    /**
     * 根据会话 ID 查询当前最新（status='current'）的 assistant 记录
     */
    List<AIConversationTurn> selectCurrentByConversationId(String conversationId);

    /**
     * 查询某会话的最大轮次
     */
    Integer selectMaxRound(String conversationId);

    /**
     * 根据会话 ID 和轮次查询记录
     */
    List<AIConversationTurn> selectByConversationIdAndRound(
            String conversationId, Integer round);

    /**
     * 将会话中所有指定轮次之后（包括该轮次）的记录 status 改为 'draft'
     */
    int updateStatusToDraftFromRound(
            String conversationId, Integer round);

    /**
     * 将会话中所有指定轮次及其之后的记录删除
     */
    int deleteFromRound(String conversationId, Integer round);

    /**
     * 将会话中指定记录的 status 和 plugin_id 更新
     */
    int updateStatus(@Param("id") String id, @Param("status") String status, @Param("pluginId") String pluginId);

    /**
     * 根据 pluginId 查询已发布的记录（status='published'）
     */
    AIConversationTurn selectPublishedByPluginId(String pluginId);

    /**
     * 查询用户的所有会话 ID 列表（去重）
     */
    List<String> selectDistinctConversationIds(String userId);
}
