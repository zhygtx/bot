package com.example.demo.pojo.task;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

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
    @JsonProperty("id")
    private String id;

    /**
     * 动作名称
     */
    private String name;

    /**
     * 所属规则ID
     */
    @JsonProperty("roleId")
    private String roleId;

    /**
     * 所属用户ID
     */
    @JsonProperty("userId")
    private String userId;

    /**
     * 是否需要@触发的用户
     */
    @Builder.Default
    @JsonProperty("needAt")
    private boolean needAt = false;

    /**
     * 动作类型，文本/图片/脚本/HTTP 调用等
     */
    @JsonProperty("actionType")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private ActionType actionType;

    /**
     * 动作消息相关数据id（根据 type 解析）
     */
    @JsonProperty("dataId")
    private String dataId;

    /**
     * 是否拼接该动作链中上一条所需发送的消息
     */
    @Builder.Default
    @JsonProperty("isConcat")
    private boolean isConcat = false;

    /**
     * 动作执行优先级，越小越先执行
     */
    @Builder.Default
    @JsonProperty("seq")
    private int seq = 0;

    /**
     * 动作类型枚举
     */
    public enum ActionType {
        text,image,api,url,template
    }

    /**
     * 提取的文本内容
     */
    private transient List<String> extractText;
}
