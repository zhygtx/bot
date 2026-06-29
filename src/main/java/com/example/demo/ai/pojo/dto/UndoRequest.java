package com.example.demo.ai.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 撤销对话轮次请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UndoRequest {

    /** 会话 ID */
    private String conversationId;

    /** 回滚目标轮次 */
    private Integer targetRound;
}
