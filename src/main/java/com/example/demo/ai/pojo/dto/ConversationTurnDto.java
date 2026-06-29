package com.example.demo.ai.pojo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 对话历史展示 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationTurnDto {

    /** 轮次 */
    private Integer round;

    /** 'user' | 'assistant' */
    private String role;

    /** 内容（用户文本或 AI 代码 JSON） */
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
