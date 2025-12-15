package com.example.demo.pojo.task.actionContent;

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
    private String id;

    /**
     * 所属用户ID
     */
    private String userId;

    /**
     * api名称
     */
    private apiName name;

    /**
     * 可调用的 api 名称
     */
    public enum apiName {
        setGroupSpecialTitle
    }
}
