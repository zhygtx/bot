package com.example.demo.ai.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 删除指定轮次及其之后的对话内容。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteRoundRequest {

    /** 会话 ID */
    private String conversationId;

    /** 从该轮次开始删除 */
    private Integer round;
}
