package com.example.demo.pojo.task;

import lombok.*;
import java.util.Map;

/**
 * 动作消息实体 支持多种动作类型并可链式执行
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Action {
    /**
     * 动作ID
     */
    private String id;

    /**
     * 动作类型，文本/图片/脚本/HTTP 调用等
     */
    private ActionType actionType;

    /**
     * 动作消息相关数据id（根据 type 解析）
     */
    private String dataId;

    /**
     * 动作执行优先级，越小越先执行
     */
    private int seq = 0;

    public enum ActionType {
        TEXT,IMAGE,ALBUM,TEMPLATE
    }
}
