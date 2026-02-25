package com.example.demo.pojo.plugin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 参数信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParameterInfo {

    /**
     * 参数id
     */
    private String id;

    /**
     * 参数描述
     */
    private String description;

    /**
     * 参数所属方法id
     */
    private String methodId;

    /**
     * 参数名
     */
    private String name;

    /**
     * 参数类型
     */
    private String type;

}
