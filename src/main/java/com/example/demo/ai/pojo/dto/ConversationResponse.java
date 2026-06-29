package com.example.demo.ai.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 对话详情响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationResponse {

    /** 会话 ID */
    private String conversationId;

    /** 关联的插件 ID */
    private String pluginId;

    /** 当前轮次 */
    private Integer currentRound;

    /** 历史对话记录 */
    private List<ConversationTurnDto> turns;
}
