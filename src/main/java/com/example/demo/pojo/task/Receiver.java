package com.example.demo.pojo.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Receiver {

    /**
     * UUID
     */
    @JsonProperty("id")
    private String id;

    /**
     * 所属用户id
     */
    @JsonProperty("userId")
    private String userId;

    /**
     * 接收动作id
     */
    @JsonProperty("actionId")
    private String actionId;

    /**
     * 发送对象类型
     */
    @JsonProperty("receiverType")
    private ReceiverType receiverType = ReceiverType.Default;//默认为默认

    /**
     * 群号
     */
    @JsonProperty("receiverGroupQQ")
    private Long receiverGroupQQ;

    /**
     * 用户QQ
     */
    @JsonProperty("receiverUserQQ")
    private Long receiverUserQQ;

    /**
     * 接收角色枚举
     */
    public enum ReceiverType {
        Private,//私聊
        Group,//群聊
        Default//默认
    }
}
