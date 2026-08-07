package com.generalbot.bot.handler;

import com.generalbot.workflow.engine.TriggerRegistry;
import com.github.zhygtx.napcat.event.BaseEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * BOT 事件处理器：沿事件继承链触发注册表中的工作流。
 */
@Slf4j
@Component
public class BotWorkflowHandler {

    private final TriggerRegistry triggerRegistry;

    /**
     * 同一事件对象因 SDK 继承分发被多次回调时去重。
     */
    private final Map<BaseEvent, Boolean> processedEvents =
            Collections.synchronizedMap(new WeakHashMap<>());

    public BotWorkflowHandler(TriggerRegistry triggerRegistry) {
        this.triggerRegistry = triggerRegistry;
    }

    /**
     * 处理 BOT 事件。
     * @param botQQ BOT QQ 号
     * @param event 事件对象
     */
    public void handleBotEvent(Long botQQ, BaseEvent event) {
        if (processedEvents.putIfAbsent(event, true) != null) {
            return;
        }
        try {
            Set<String> seenIds = new HashSet<>();
            Class<?> clazz = event.getClass();
            while (clazz != null && BaseEvent.class.isAssignableFrom(clazz)) {
                String triggerKey = "botEvent:" + botQQ + ":" + clazz.getSimpleName();
                triggerRegistry.fire(triggerKey, event, seenIds);
                clazz = clazz.getSuperclass();
            }
        } catch (Exception e) {
            log.error("处理 BOT 事件异常", e);
        }
    }
}
