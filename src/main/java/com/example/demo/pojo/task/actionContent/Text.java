package com.example.demo.pojo.task.actionContent;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文本消息实体，定义所发送的内容
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Text {

    /**
     * 文本消息ID
     */
    @JsonProperty("id")
    private String id;

    /**
     * 文本名称
     */
    @JsonProperty("name")
    private String name;

    /**
     * 所属用户ID
     */
    @JsonProperty("userId")
    private String userId;

    /**
     * 文本内容
     */
    @JsonProperty("text")
    private String text;

}
