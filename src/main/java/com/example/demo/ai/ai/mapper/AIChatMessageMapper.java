package com.example.demo.ai.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.ai.ai.pojo.dto.AIChatMessageDto;
import com.example.demo.ai.ai.pojo.entity.AIChatMessage;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AIChatMessageMapper extends BaseMapper<AIChatMessage> {

    /**
     * 根据用户 ID 查询所有消息记录。
     * @param userId 用户 ID
     * @param offset 偏移量（从 0 开始）
     * @param count  查询数量
     * @return 消息记录列表
     */
    List<AIChatMessageDto> selectByUserId(@Param("userId") String userId, @Param("offset") Integer offset, @Param("count") Integer count);

    @Select("select * from ai_chat_message where conversation_id = #{conversationId} and role = 'assistant' order by round desc limit 1")
    AIChatMessage selectLastCode(String conversationId);

    /**
     * 删除会话 ID 小于等于指定轮次的所有消息记录。
     * @param conversationId 会话 ID
     * @param round          轮次
     */
    @Delete("delete from ai_chat_message where conversation_id = #{conversationId} and round < #{round}")
    void deleteByConversationIdAndRound(String conversationId, Integer round);

    /**
     * 更新会话 ID 所有消息记录的状态为已发布（round=1）。
     * @param conversationId 会话 ID
     * @param status         状态
     */
    @Update("update ai_chat_message set status = #{status}, round = 1, plugin_id = #{pluginId} where conversation_id = #{conversationId}")
    void updateStatusAndRound(String conversationId, String pluginId, AIChatMessage.Status status);

    /**
     * 更新指定消息记录的文本内容。
     * @param id         消息记录 ID
     * @param message    新的文本内容
     */
    @Update("update ai_chat_message set message = #{message} where id = #{id}")
    void updateMessage(String id, String message);
}
