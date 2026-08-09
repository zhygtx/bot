package com.generalbot.bot.handler;

import com.generalbot.workflow.engine.TriggerRegistry;
import com.github.zhygtx.napcat.event.RawBotEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * BOT 事件处理器：沿事件目录中的父类型链触发工作流。
 */
@Slf4j
@Component
public class BotWorkflowHandler {

    private final TriggerRegistry triggerRegistry;

    /**
     * 同一事件对象被多次分发时去重。
     */
    private final Map<RawBotEvent, Boolean> processedEvents =
            Collections.synchronizedMap(new WeakHashMap<>());

    public BotWorkflowHandler(TriggerRegistry triggerRegistry) {
        this.triggerRegistry = triggerRegistry;
    }

    /**
     * 处理通用 BOT 事件。
     *
     * @param botQQ BOT QQ 号
     * @param event 通用事件载体
     */
    public void handleBotEvent(Long botQQ, RawBotEvent event) {
        if (processedEvents.putIfAbsent(event, true) != null) {
            return;
        }
        try {
            Set<String> seenIds = new HashSet<>();
            Map<String, Object> payload = event.toDataMap();
            List<String> eventTypes = new ArrayList<>();
            eventTypes.add(event.getEventType());
            if (event.getParentEventTypes() != null) {
                eventTypes.addAll(event.getParentEventTypes());
            }
            for (String eventType : eventTypes) {
                triggerRegistry.fire("botEvent:" + botQQ + ":" + eventType, payload, seenIds);
            }
        } catch (Exception e) {
            log.error("处理 BOT 事件异常", e);
        }
    }
}
