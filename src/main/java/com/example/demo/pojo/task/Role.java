package com.example.demo.pojo.task;

import com.example.demo.interceptor.ExcludeFromMD5;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.regex.Pattern;


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
     * 所属作用域ID，UUID
     */
    private String scopeId;

    /**
     * 触发规则MD5值
     */
    @ExcludeFromMD5
    private String MD5;

    /**
     * 匹配类型
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private MatchMode matchMode;

    /**
     * 正则表达式内容
     */
    private String regex;

    /**
     * 是否启用
     */
    @Builder.Default
    private boolean isEnable = false;

    /**
     * 是否提取正则表达式后续文本
     */
    @Builder.Default
    private boolean isExtract = false;

    /**
     * 提取文本位置
     */
    private List<Integer> extractPosition;

    /**
     * 匹配类型
     */
    public enum MatchMode {
        text,
        image
    }

    /**
     * 关联的动作内容
     */
    private transient List<Action> action;

    /**
     * 预编译的Pattern对象
     */
    @ExcludeFromMD5
    private transient Pattern pattern;

    /**
     * 获取预编译的Pattern对象
     * @return Pattern对象
     */
    public Pattern getPattern() {
        if (pattern == null && regex != null && !regex.isEmpty()) {
            pattern = Pattern.compile(regex);
        }
        return pattern;
    }
}
