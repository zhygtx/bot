package com.example.demo.ai.ai.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
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

    /** 'user' | 'assistant' */
    private String role;

    /**
     * 用户指令文本（role=user）或 JSON 格式代码数组（role=assistant）
     */
    private String message;

    /**
     * 生成的代码JSON格式（role=assistant）
     * 格式：{[{"path":"path","code":"code"},...]}
     */
    private String code;

    /**
     * 'draft': 历史轮次（已被后续覆盖）
     * 'current': 当前最新未发布版本
     * 'published': 已编译上传的版本
     */
    private Status status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    public enum Status {
        DRAFT,//历史轮次（已被后续覆盖）
        CURRENT,//当前最新未发布版本
        PUBLISHED//已编译上传的版本
    }

}
