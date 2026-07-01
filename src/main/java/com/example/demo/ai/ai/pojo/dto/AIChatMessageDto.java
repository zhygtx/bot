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
     * 'draft': 历史轮次（已被后续覆盖）
     * 'current': 当前最新未发布版本
     * 'published': 已编译上传的版本
     */
    private AIChatMessage.Status status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
