package com.generalbot.workflow.engine;

import com.generalbot.plugin.entity.ParameterInfo;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 统一 callable 描述：系统事件、定时、BOT 动作、插件方法都注册成该结构。
 */
@Data
@Builder
public class CallableDescriptor {

    /**
     * callable 引用 key
     */
    private String key;

    /**
     * 显示名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 角色：TRIGGER 为触发节点，TASK 为普通任务节点
     */
    private CallableKind kind;

    /**
     * 来源：系统或插件
     */
    private CallableSource source;

    /**
     * 返回值类型
     */
    private String returnType;

    /**
     * 参数列表
     */
    private List<ParameterInfo> parameters;

    /**
     * 返回值可映射字段列表，供前端做数据来源选择与类型校验
     */
    private List<CallableField> returnFields;

    /**
     * 触发类型：botEvent/schedule
     */
    private String triggerType;

    /**
     * 附加元数据
     */
    private Map<String, Object> metadata;

    /**
     * callable 角色
     */
    public enum CallableKind {
        TRIGGER,
        TASK
    }

    /**
     * callable 来源
     */
    public enum CallableSource {
        SYSTEM,
        PLUGIN
    }
}
