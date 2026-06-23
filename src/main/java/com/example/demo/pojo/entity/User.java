package com.example.demo.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户信息
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class User {
    /**
     * 用户ID
     */
    private String id;

    /**
     * 用户名
     */
    private String name;

    /**
     * 用户账号
     */
    private String account;

    /**
     * 密码
     */
    private String pwd;

    /**
     * QQ号
     */
    @JsonProperty("QQ")
    private Long QQ;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户权限(给予管理员时手动填写，默认空时为普通用户)
     */
    private String userRole;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 最后修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}