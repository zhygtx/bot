package com.example.demo.pojo.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 任务触发域实体类
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Scope {

    /**
     * 触发规则ID，UUID
     */
    private String id;

    /**
     * 触发规则名称
     */
    private String name;

    /**
     * 机器人QQ
     */
    private Long botQQ;

    /**
     * 所属用户ID
     */
    private String userId;

    /**
     * 是否需要@才可触发
     */
    private boolean isAt = true;

    /**
     * 所拥有的触发规则
     */
    private List<Role> roles;

    /**
     * 作用用户所需权限
     */
    private ScopeUserRole scopeUserRole;

    /**
     * 作用用户权限枚举
     */
    public enum ScopeUserRole {
        ALL,
        ADMIN,
        OWNER,
        MEMBER
    }
}
