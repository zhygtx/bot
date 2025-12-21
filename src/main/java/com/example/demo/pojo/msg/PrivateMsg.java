package com.example.demo.pojo.msg;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * 私聊消息实体
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class PrivateMsg extends Msg{

}
