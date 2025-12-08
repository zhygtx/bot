package com.example.demo.pojo.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

/**
 * 任务触发规则
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    /**
     * 触发规则ID，UUID
     */
    private String id;

    /**
     * 触发规则名称
     */
    private String name;

    /**
     * 机器人QQ
     */
    private Long botQQ;

    /**
     * 所属用户ID
     */
    private String userId;

    /**
     * 触发规则MD5值
     */
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
    private boolean isEnable;

    /**
     * 是否提取正则表达式后续文本
     */
    private boolean isExtract;

    /**
     * 匹配模式，正则匹配与文本等于匹配
     */
    private enum MatchMode {
        REGEX,
        TEXT
    }
}
