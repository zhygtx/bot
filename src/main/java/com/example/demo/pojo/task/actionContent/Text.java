package com.example.demo.pojo.task.actionContent;

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
     * 文本内容
     */
    private String text;

}
