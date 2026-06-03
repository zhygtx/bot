package com.example.demo.pojo.entity.plugin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实体类属性信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Attribute {

    /**
     * 属性id
     * 系统生成系统与用户均不可修改
     */
    private String id;

    /**
     * 属性描述
     * 系统生成默认为空用户可修改
     */
    private String description;

    /**
     * 属性所属实体类id
     * 系统生成系统与用户均不可修改
     */
    private String entityInfoId;

    /**
     * 属性类型
     * 系统生成系统与用户均不可修改
     */
    private String type;

    /**
     * 属性名称
     * 系统生成系统与用户均不可修改
     */
    private String name;

}
