package com.example.demo.pojo.task.actionContent;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Api {

    /**
     * apiID
     */
    @JsonProperty("id")
    private String id;

    /**
     * 所属用户ID
     */
    @JsonProperty("userId")
    private String userId;

    /**
     * api名称
     */
    @JsonProperty("name")
    private String name;

    /**
     * api
     */
    @JsonProperty("apiType")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private apiType apiType;

    /**
     * 参数<属性名，属性值>
     */
    @JsonProperty("params")
    private transient Map<String, String> params;

    /**
     * 可调用的 api 名称
     */
    public enum apiType {
        setGroupSpecialTitle,
        getWarframeFissure
    }
}
