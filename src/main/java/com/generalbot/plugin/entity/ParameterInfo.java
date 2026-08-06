package com.generalbot.plugin.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * 参数信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParameterInfo {

    // 允许的参数类型集合
    private static final Set<String> ALLOWED_TYPES = Set.of(
        // 基本数据类型
        "byte", "short", "int", "long", "float", "double", "char", "boolean",
        // 包装类
        "Byte", "Short", "Integer", "Long", "Float", "Double", "Character", "Boolean",
        // 常用类
        "String", "Object",
        // 集合类型
        "java.util.List", "java.util.ArrayList", "java.util.LinkedList",
        "java.util.Set", "java.util.HashSet", "java.util.TreeSet",
        "java.util.Map", "java.util.HashMap", "java.util.TreeMap",
        "java.util.Collection", "java.util.Queue", "java.util.ArrayDeque"
    );

    // Java 基本数据类型集合（不能标记为 nullable=true）
    private static final Set<String> PRIMITIVE_TYPES = Set.of(
        "byte", "short", "int", "long", "float", "double", "char", "boolean"
    );

    /**
     * 参数 id
     * 系统生成系统与用户均不可修改
     */
    private String id;

    /**
     * 参数顺序
     * 系统生成
     */
    private Integer order;

    /**
     * 参数描述
     * 系统生成默认为空用户可修改
     */
    private String description;

    /**
     * 参数所属方法id
     * 系统生成系统与用户均不可修改
     */
    private String methodId;

    /**
     * 参数名
     * 系统生成系统与用户均不可修改
     */
    private String name;

    /**
     * 参数类型
     * 系统生成系统与用户均不可修改
     */
    private String type;

    /**
     * 参数是否允许为 null。
     * 默认不允许，从前端 @ActionParam(nullable=...) 读取。
     */
    @Builder.Default
    private boolean nullable = false;

    /**
     * 复杂类型参数平铺后的子字段列表
     * 当 type 为自定义 POJO 时，将类中的字段平铺展示
     */
    @Builder.Default
    private List<ParameterFieldInfo> children = null;

    /**
     * 校验参数类型是否合法
     * 只允许基本数据类型、包装类、String、Object 和常用集合类型
     * @return 校验结果，true 表示合法，false 表示不合法
     */
    public boolean isValidType() {
        if (this.type == null || this.type.trim().isEmpty()) {
            return false;
        }
        
        // 去除泛型部分，只保留原始类型
        String baseType = this.type.replaceAll("<.*>", "").trim();
        
        // 检查是否在允许的类型列表中
        return ALLOWED_TYPES.contains(baseType);
    }

    /**
     * 获取不允许的类型说明
     * @return 类型说明信息
     */
    public String getInvalidTypeMessage() {
        if (isValidType()) {
            return "";
        }
        return String.format("参数类型 '%s' 不支持，只允许基本数据类型、包装类、String、Object 和常用集合类型", this.type);
    }

    /**
     * 校验 nullable 标记是否合法
     * 基本数据类型（int、long 等）不能标记为 nullable=true，
     * 因为 Java 基本类型在反射调用时不能接收 null 值
     * @return 校验结果，true 表示合法，false 表示不合法
     */
    public boolean isValidNullable() {
        if (!this.nullable) {
            return true;
        }
        return !PRIMITIVE_TYPES.contains(this.type);
    }

    /**
     * 获取 nullable 校验不通过的原因说明
     * @return 原因说明信息
     */
    public String getInvalidNullableMessage() {
        if (isValidNullable()) {
            return "";
        }
        return String.format("参数 '%s' 是基本数据类型 '%s'，不能标记为可空（nullable=true），基本类型不支持 null 值",
                this.name, this.type);
    }

}
