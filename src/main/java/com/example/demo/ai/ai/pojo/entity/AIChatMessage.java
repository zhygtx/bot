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
@TableName("ai_chat_message")
public class AIChatMessage {

    /** 记录主键（UUID，每条记录唯一） */
    private String id;

    /** 会话 ID（同一对话中所有记录共享此值） */
    private String conversationId;

    /** 关联的插件 ID（首次生成时为 null） */
    private String pluginId;

    /** 创建者用户 ID */
    private String userId;

    /** 对话轮次，从 1 开始 */
    private Integer round;

    /** 'user' | 'assistant' */
    private String role;

    /**
     * 用户指令文本（role=user）或 JSON 格式代码数组（role=assistant）
     */
    private String message;

    /**
     * 生成的代码JSON格式（role=assistant）
     * 格式：{[{"path":"path","code":"code"},...]}
     */
    private String code;

    /**
     * 'draft': 未发布（从未发布过）
     * 'published': 已发布
     * 'published_draft': 更新未发布（曾经发布过，当前又有改动未发布）
     */
    private Status status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    public enum Status {
        DRAFT,             // 未发布（草稿）
        PUBLISHED,         // 已发布
        PUBLISHED_DRAFT    // 更新未发布（曾经发布过，当前又有改动未发布）
    }

}
