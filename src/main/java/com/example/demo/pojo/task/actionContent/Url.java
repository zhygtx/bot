package com.example.demo.pojo.task.actionContent;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("id")
    private String id;

    /**
     * 所属用户ID
     */
    @JsonProperty("userId")
    private String userId;

    /**
     * url名称
     */
    @JsonProperty("name")
    private String name;

    /**
     * url
     */
    @JsonProperty("url")
    private String url;

    /**
     * URL参数
     */
    @Builder.Default
    @JsonProperty("params")
    private Map<String,String> params = new HashMap<>();
}
