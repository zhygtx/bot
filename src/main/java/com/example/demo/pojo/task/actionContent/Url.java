package com.example.demo.pojo.task.actionContent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Url {
    /**
     * url id
     */
    private String id;

    /**
     * 所属用户ID
     */
    private String userId;

    /**
     * url
     */
    private String url;

    /**
     * URL参数
     */
    private Map<String,String> params = new HashMap<>();
}
