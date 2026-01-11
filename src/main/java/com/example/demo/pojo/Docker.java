package com.example.demo.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Docker {

    /**
     * 容器ID
     */
    private String id;

    /**
     * 容器名称
     */
    private String name;

    /**
     * 容器所属用户
     */
    private String userId;

    /**
     * 容器外部映射端口
     */
    private String port;

    /**
     * 容器token(暂不支持后续修改)
     */
    private String token;

}
