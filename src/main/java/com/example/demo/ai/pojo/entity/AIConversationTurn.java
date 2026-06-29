package com.example.demo.ai.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 对话轮次与代码存储
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIConversationTurn {

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
    private String content;

    /**
     * 'draft': 历史轮次（已被后续覆盖）
     * 'current': 当前最新未发布版本
     * 'published': 已编译上传的版本
     */
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
