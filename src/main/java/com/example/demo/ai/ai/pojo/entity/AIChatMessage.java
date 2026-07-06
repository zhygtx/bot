package com.example.demo.ai.ai.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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

    /**
     * 用户指令文本
     */
    private String userMessage;

    /**
     * AI 回复文本
     */
    private String aiMessage;

    /**
     * 'draft': 未发布（从未发布过）
     * 'published': 已发布
     * 'published_draft': 更新未发布（曾经发布过，当前又有改动未发布）
     */
    private Status status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 代码依赖
     */
    private String pom;

    /**
     * 插件名称
     */
    private String pluginName;

    /**
     * 插件介绍
     */
    private String pluginDescription;

    /**
     * 版本号
     */
    private String version;

    /**
     * 是否公开
     */
    private Boolean isPublic;

    /**
     * 版本变更说明
     */
    private String changelog;

    /**
     * 代码变更列表
     */
    @TableField(exist = false)
    private List<Code> codes;

    public enum Status {
        DRAFT,             // 未发布（草稿）
        PUBLISHED,         // 已发布
        PUBLISHED_DRAFT    // 更新未发布（曾经发布过，当前又有改动未发布）
    }

}
