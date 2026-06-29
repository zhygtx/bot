package com.example.demo.ai.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 生成的源码文件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SourceFile {
    /** 文件路径，如 src/main/java/com/example/entity/PointsData.java */
    private String filePath;

    /** Java 源码内容 */
    private String content;
}
