package com.example.demo.pojo.task;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
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
    @JsonProperty("id")
    private String id;

    /**
     * 触发规则名称
     */
    @JsonProperty("name")
    private String name;

    /**
     * 所属用户ID
     */
    @JsonProperty("userId")
    private String userId;

    /**
     * 机器人QQ
     */
    @JsonProperty("botQQ")
    private Long botQQ;

    /**
     * 是否需要@才可触发
     */
    @JsonProperty("isAt")
    @Builder.Default
    private boolean isAt = true;

    /**
     * 作用用户所需权限
     */
    @JsonProperty("qqUserRole")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private GroupRole qqUserRole;

    /**
     * 所需Bot权限
     */
    @JsonProperty("qqBotRole")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private GroupRole qqBotRole;

    /**
     * 作用域类型
     */
    @JsonProperty("qqScopeType")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private ScopeType qqScopeType;

    /**
     * 作用对象号(私聊为qq号，群聊为群号)
     */
    @JsonProperty("qqScopeId")
    private Long qqScopeId;

    /**
     * 作用域类型
     */
    public enum ScopeType {
        All,
        Group,
        Private
    }
    //todo 字段内容修改

    /**
     * 所拥有的触发规则
     */
    private transient List<Role> roles = new ArrayList<>();

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
            ROLE_LEVELS.put(all, 0);
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
            return ROLE_LEVELS.get(this) <= requiredLevel;
        }
    }

}
