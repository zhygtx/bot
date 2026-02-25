package com.example.demo.pojo.workflow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 条件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Condition {

    /**
     * 条件ID
     */
    private String id;

    /**
     * 所属工作流ID
     */
    private String workflowId;

    /**
     * 所判断的数据产生者节点ID
     */
    private String nodeId;

    /**
     * 判断的数据字段名称
     */
    private String fieldName;

    /**
     * 预设内容（位于比较右侧）
     */
    private String presetContent;

    /**
     * 预设内容类型
     */
    private ContentType contentType;

    /**
     * 满足条件时执行内容
     */
    private Action action;

    /**
     * 不满足条件时执行内容
     */
    private Action elseAction;

    /**
     * 判断条件操作符
     */
    private Operator operator;

    /**
     * 预设内容类型
     */
    public enum ContentType {
        STRING,//字符串
        NUMBER,//数字
        BOOLEAN//布尔值
    }

    /**
     * 判断条件枚举
     */
    @Getter
    public enum Operator {
        EQUALS("等于"),              // =
        NOT_EQUALS("不等于"),         // !=
        GREATER_THAN("大于"),         // >
        GREATER_THAN_OR_EQUALS("大于等于"), // >=
        LESS_THAN("小于"),           // <
        LESS_THAN_OR_EQUALS("小于等于"),  // <=
        CONTAINS("包含"),            // contains
        NOT_CONTAINS("不包含"),       // not contains
        REGEX("正则匹配"),           // regex (仅用于字符串模式匹配)
        IS_NULL("为空"),             // is null
        IS_NOT_NULL("不为空");        // is not null

        private final String description;

        Operator(String description) {
            this.description = description;
        }
    }

    /**
     * 满足条件时执行内容
     */
    public enum Action {
        CONTINUE,//继续执行
        BREAK,//结束词条分支，不再执行后续节点
        END//结束整个工作流
    }

}
