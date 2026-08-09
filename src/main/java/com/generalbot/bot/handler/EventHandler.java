package com.generalbot.bot.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.zhygtx.napcat.event.EventFilter;
import com.github.zhygtx.napcat.event.GenericBotEventListener;
import com.github.zhygtx.napcat.event.RawBotEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * 通用 BOT 事件入口。
 * <p>
 * 接收 SDK 的 {@link RawBotEvent}，统一交给 {@link BotWorkflowHandler} 触发工作流。
 */
@Slf4j
@Component
public class EventHandler implements GenericBotEventListener {

    private final BotWorkflowHandler botWorkflowHandler;

    public EventHandler(BotWorkflowHandler botWorkflowHandler) {
        this.botWorkflowHandler = botWorkflowHandler;
    }

    /**
     * 过滤掉 Bot 自己接收到的消息，保留 message_sent 发送事件。
     */
    @Bean
    public EventFilter botFilter() {
        return (botQQ, event) -> {
            if (!(event instanceof RawBotEvent raw)) {
                return true;
            }
            String key = raw.getEventKey();
            if (key == null || !key.startsWith("message")) {
                return true;
            }
            if (key.startsWith("message_sent")) {
                return true;
            }
            JsonNode payload = raw.getPayload();
            if (payload == null || botQQ == null) {
                return true;
            }
            return payload.path("user_id").asLong(-1) != botQQ;
        };
    }

    @Override
    public void onGenericEvent(Long botQQ, RawBotEvent event) {
        botWorkflowHandler.handleBotEvent(botQQ, event);
    }
}
