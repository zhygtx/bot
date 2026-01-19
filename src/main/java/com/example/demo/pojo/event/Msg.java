package com.example.demo.pojo.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Msg extends Event{
    /**
     * 消息具体内容
     */
    private Map<Integer, Map<String, Object>> content;

    /**
     * 消息类型
     */
    private List<String> type;
}
