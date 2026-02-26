package com.example.demo.pojo.plugin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实体类信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityInfo {
    /**
     * 实体类id
     */
    private String id;

    /**
     * 实体类描述
     */
    private String description;

    /**
     * 实体类简写名称
     */
    private String name;

    /**
     * 实体类所属插件版本id
     */
    private String versionId;

    /**
    * 实体全限定名
    */
    private String entityName;

    /**
     * 实体类属性信息(JSON格式)
     */
    private String attributes;
}
