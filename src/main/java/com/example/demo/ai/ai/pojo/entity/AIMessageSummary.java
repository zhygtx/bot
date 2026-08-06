package com.example.demo.ai.ai.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("ai_message_summary")
public class AIMessageSummary {

    /** 记录主键（UUID，每条记录唯一） */
    private String id;

    /** 会话 ID（同一对话中所有记录共享此值） */
    private String conversationId;

    /** 创建者用户 ID */
    private String userId;

    /** 被压缩的最后一轮 */
    private Integer summaryRound;

    /** 指向 summary_round 那一轮的消息 ID（外键，关联 ai_chat_message.id） */
    private String messageId;

    /** 压缩结果 JSON */
    private String summaryContent;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
