package com.generalbot.theme.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户自定义主题。
 * 这里保存的是主题快照，而不是 CSS 文件，因此后续编辑、导入导出和数据库同步都更直接。
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserTheme {

    /**
     * 主题 ID。
     */
    private String id;

    /**
     * 所属用户 ID。
     */
    private String userId;

    /**
     * 主题名称。
     */
    private String name;

    /**
     * 主题模式，light / dark。
     */
    private String mode;

    /**
     * 主题令牌 JSON。
     * 前端编辑器展示的是结构化表单，落库时统一转成 JSON 文本。
     */
    private String tokens;

    /**
     * 创建时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 最后修改时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
