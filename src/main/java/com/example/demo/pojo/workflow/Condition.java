package com.example.demo.pojo.workflow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 条件
 * 基于插件返回的布尔值进行判断
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
     * 所判断的节点ID（插件节点）
     */
    private String nodeId;

    /**
     * 插件返回true时执行内容
     */
    private Action trueAction;

    /**
     * 插件返回false时执行内容
     */
    private Action falseAction;

    /**
     * 执行动作枚举
     */
    public enum Action {
        CONTINUE,//继续执行
        BREAK,//结束当前分支，不再执行后续节点
        END//结束整个工作流
    }

}
