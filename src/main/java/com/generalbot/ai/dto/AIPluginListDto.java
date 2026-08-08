package com.generalbot.ai.dto;

import com.generalbot.ai.entity.AIChatMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 生成插件会话列表 DTO。
 * 只返回列表卡片展示所需的轻量字段，避免把整条消息内容和代码数据带回前端。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AIPluginListDto {

    /** 会话 ID（同一对话中所有记录共享此值） */
    private String conversationId;

    /** 插件名称 */
    private String pluginName;

    /** 插件描述 */
    private String pluginDescription;

    /** 会话状态：DRAFT / PUBLISHED / PUBLISHED_DRAFT */
    private AIChatMessage.Status status;

    /** 更新时间（取会话内最新一条消息的创建时间） */
    private LocalDateTime updateTime;
}
