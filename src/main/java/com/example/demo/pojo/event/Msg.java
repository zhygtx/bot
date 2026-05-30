package com.example.demo.pojo.event;

import com.example.demo.annotation.EventField;
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
    @EventField(description = "消息具体内容", order = 6)
    private Map<Integer, Map<String, Object>> content;

    /**
     * 消息类型
     */
    @EventField(description = "消息类型", order = 7)
    private List<String> type;
}
