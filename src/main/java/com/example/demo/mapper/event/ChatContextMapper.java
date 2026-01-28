package com.example.demo.mapper.event;

import com.example.demo.pojo.event.ChatContext;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChatContextMapper {

    /**
     * 插入一条聊天上下文
     * @param chatContext 聊天上下文
     */
    @Insert("INSERT INTO chat_context (id,bot_id, sender_type, group_id, user_id, msg_type, msg, is_summary, summary_id, use_token, time) " +
            "VALUES (#{id},#{botId}, #{senderType}, #{groupId}, #{userId}, #{msgType}, #{msg}, #{isSummary}, #{summaryId}, #{useToken}, #{time})")
    void insert(ChatContext chatContext);

    /**
     * 判断群组是否存在总结
     * @param groupId 群组ID
     * @param botId 机器人ID
     * @return 是否存在总结
     */
    @Select("SELECT * FROM chat_context WHERE " +
            "group_id = #{groupId} AND bot_id = #{botId} AND is_summary = 1 ORDER BY time DESC LIMIT 1")
    ChatContext hasGroupSummary(Long groupId, Long botId);

    /**
     * 判断用户是否存在总结
     * @param userId 用户ID
     * @param botId 机器人ID
     * @return 是否存在总结
     */
    @Select("SELECT * FROM chat_context where " +
            "group_id is null and user_id = #{userId} AND bot_id = #{botId} AND is_summary = 1 ORDER BY time DESC LIMIT 1")
    ChatContext hasUserSummary(Long userId, Long botId);

    /**
     * 获取群组上下文
     * @param groupId 群组ID
     * @param botId 机器人ID
     * @return 群组上下文
     */
    @Select("SELECT * FROM chat_context WHERE group_id = #{groupId} and bot_id = #{botId} ORDER BY time ")
    List<ChatContext> getByGroupId(Long groupId, Long botId);

    /**
     * 获取用户上下文
     * @param userId 用户ID
     * @param botId 机器人ID
     * @return 用户上下文
     */
    @Select("SELECT * FROM chat_context WHERE group_id is null and user_id = #{userId} and bot_id = #{botId} ORDER BY time ")
    List<ChatContext> getByUserId(Long userId, Long botId);


    /**
     * 获取同总结群组
     * @param summaryId 总结ID
     * @return 群组总结
     */
    @Select("SELECT * FROM chat_context WHERE summary_id = #{summaryId} ORDER BY time ")
    List<ChatContext> getBySummaryId(String summaryId);

    /**
     * 更新上下文
     * @param chatContexts 上下文
     */
    void update(@Param("chatContexts") List<ChatContext> chatContexts, @Param("summaryId") String summaryId);
}
