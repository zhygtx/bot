package com.example.demo.ai.ai.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 回复中一次真实工具调用的持久化记录。
 * 前端通过它还原历史消息里的工具调用卡片。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_tool_call_record")
public class AIToolCallRecord {

    private String id;

    private String conversationId;

    private String assistantMessageId;

    private Integer round;

    private Integer sequence;

    private Integer partIndex;

    private String method;

    private String displayName;

    private String description;

    private String category;

    /** 参数预览 JSON，统一由前端工具卡片渲染。 */
    private String argumentsPreviewJson;

    /** 结果预览 JSON，统一由前端工具卡片渲染。 */
    private String resultPreviewJson;

    private String status;

    private String errorMessage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime finishedAt;

    private Long durationMs;
}
