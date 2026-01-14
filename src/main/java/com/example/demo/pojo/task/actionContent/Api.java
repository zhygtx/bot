package com.example.demo.pojo.task.actionContent;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private apiName name;

    /**
     * 可调用的 api 名称
     */
    public enum apiName {
        setGroupSpecialTitle,
        getWarframeFissure
    }
}
