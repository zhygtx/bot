package com.example.demo.ai.ai.pojo.dto;

import com.example.demo.ai.ai.pojo.entity.AIChatMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AIChatMessageDto {

    /** 会话 ID（同一对话中所有记录共享此值） */
    private String conversationId;

    /** 关联的插件 ID（首次生成时为 null） */
    private String pluginId;

    /** 关联的插件名称（首次生成时为 null） */
    private String pluginName;

    /**
     * 'draft': 未发布（从未发布过）
     * 'published': 已发布
     * 'published_draft': 更新未发布（曾经发布过，当前又有改动未发布）
     */
    private AIChatMessage.Status status;

    /** 最后一次对话的代码（首次生成时为 null） */
    private String lastCode;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
