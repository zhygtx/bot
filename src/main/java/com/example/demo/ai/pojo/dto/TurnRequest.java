package com.example.demo.ai.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 微调对话轮次请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurnRequest {

    /** 会话 ID */
    private String conversationId;

    /** 用户微调指令 */
    private String instruction;

    /** 实体类包名 */
    private String entityPackage;

    /** 方法类包名 */
    private String methodPackage;
}
