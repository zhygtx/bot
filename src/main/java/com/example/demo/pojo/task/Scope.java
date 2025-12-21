package com.example.demo.pojo.task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * 作用用户所需权限
     */
    private GroupRole userRole;

    /**
     * 所需Bot权限
     */
    private GroupRole botRole;

    /**
     * 作用域类型
     */
    private ScopeType scopeType;

    /**
     * 作用域类型
     */
    public enum ScopeType {
        all,
        groupMsg,
        privateMsg
    }

    /**
     * 所拥有的触发规则
     */
    private transient List<Role> roles;

    /**
     * 作用用户权限枚举
     */
    public enum GroupRole {
        all,
        owner,
        admin,
        member;

        private static final Map<GroupRole, Integer> ROLE_LEVELS = new HashMap<>();
        private static final Map<String, Integer> STRING_ROLE_LEVELS = new HashMap<>();

        static {
            ROLE_LEVELS.put(all, 4);
            ROLE_LEVELS.put(owner, 3);
            ROLE_LEVELS.put(admin, 2);
            ROLE_LEVELS.put(member, 1);

            for (Map.Entry<GroupRole, Integer> entry : ROLE_LEVELS.entrySet()) {
                STRING_ROLE_LEVELS.put(entry.getKey().name(), entry.getValue());
            }
        }

        public boolean hasPermission(String role) {
            Integer requiredLevel = STRING_ROLE_LEVELS.get(role.toLowerCase());
            if (requiredLevel == null) {
                return false;
            }
            return ROLE_LEVELS.get(this) >= requiredLevel;
        }
    }

}
