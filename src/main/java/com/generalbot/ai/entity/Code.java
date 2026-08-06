package com.generalbot.ai.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Code {

    /**
     * 代码 ID
     */
    private String id;

    /**
     * 消息 ID
     */
    private String messageId;

    /**
     * 代码路径
     */
    private String path;

    /**
     * 代码内容
     */
    private String content;

    /**
     * 代码介绍
     */
    private String description;
}
