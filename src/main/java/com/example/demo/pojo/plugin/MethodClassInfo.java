package com.example.demo.pojo.plugin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 方法类信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MethodClassInfo {
    /**
     * 方法类id
     */
    private String id;

    /**
     * 类描述
     */
    private String description;

    /**
     * 方法类版本id
     */
    private String versionId;

    /**
     * 类全限定名
     */
    private String className;

    /**
     * 简单类名
     */
    private String simpleClassName;

    /**
     * 包名
     */
    private String packageName;

    /**
     * 该类中的方法列表
     */
    private List<MethodInfo> methods;
}