package com.example.demo.pojo.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提取位置实体类
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ExtractPosition {

    /**
     * 主键自增
     */
    private Long id;

    /**
     * 所属Role的ID
     */
    private String roleId;

    /**
     * 提取文本位置
     */
    private Integer extractPosition;

}