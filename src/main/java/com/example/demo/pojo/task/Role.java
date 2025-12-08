package com.example.demo.pojo.task;

import com.example.demo.interceptor.ExcludeFromMD5;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 触发任务规则实体
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Role {

    /**
     * 任务规则ID，UUID
     */
    @ExcludeFromMD5
    private String id;

    /**
     * 所属用户ID，UUID
     */
    private String userId;

    /**
     * 触发规则MD5值
     */
    @ExcludeFromMD5
    private String MD5;

    /**
     * 匹配方式，正则匹配与文本等于匹配
     */
    private MatchMode matchMode;

    /**
     * 匹配内容
     */
    private String matchContent;

    /**
     * 是否启用
     */
    private boolean isEnable = false;

    /**
     * 是否提取正则表达式后续文本
     */
    private boolean isExtract = false;

    /**
     * 匹配模式，正则匹配与文本等于匹配
     */
    private enum MatchMode {
        REGEX,
        TEXT
    }

}
