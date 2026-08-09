package com.generalbot.bot.scanner;

import com.generalbot.bot.metadata.EventMetadata;
import com.generalbot.bot.metadata.FieldMetadata;
import com.github.zhygtx.napcat.protocol.EventDescriptor;
import com.github.zhygtx.napcat.protocol.ProtocolCatalog;
import com.github.zhygtx.napcat.protocol.ProtocolField;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * BOT 事件元数据适配器。
 * <p>
 * 数据来源为 SDK 的 {@link ProtocolCatalog}，不再反射扫描 Java 事件类。
 */
@Component
@Slf4j
public class BotEventScanner {

    @Getter
    private final List<EventMetadata> eventMetadataList = new ArrayList<>();

    private final ProtocolCatalog protocolCatalog;

    public BotEventScanner(ProtocolCatalog protocolCatalog) {
        this.protocolCatalog = protocolCatalog;
    }

    @PostConstruct
    public void init() {
        addScheduledEvent();
        for (EventDescriptor event : protocolCatalog.getEvents()) {
            eventMetadataList.add(toMetadata(event));
        }
        eventMetadataList.sort(Comparator.comparingInt(EventMetadata::getOrder));
        log.info("BOT事件目录加载完成，共 {} 个事件", eventMetadataList.size());
    }

    private EventMetadata toMetadata(EventDescriptor event) {
        List<FieldMetadata> fields = new ArrayList<>();
        int order = 0;
        for (ProtocolField field : event.getFields()) {
            fields.add(FieldMetadata.builder()
                    .fieldName(field.getJsonName())
                    .fieldType(field.getType())
                    .description(field.getDescription())
                    .order(order++)
                    .inherited(false)
                    .required(false)
                    .example("")
                    .build());
        }
        EventMetadata.EntityMetadata entityInfo = EventMetadata.EntityMetadata.builder()
                .entityName(event.getEntityName())
                .fields(fields)
                .build();
        return EventMetadata.builder()
                .eventType(event.getEventType())
                .eventName(event.getEventType())
                .description(event.getEventType())
                .order(0)
                .categories(categoriesOf(event.getPostType()))
                .categoryOrders(List.of(0))
                .entityInfo(entityInfo)
                .build();
    }

    private List<String> categoriesOf(String postType) {
        return switch (postType) {
            case "message" -> List.of("消息事件");
            case "message_sent" -> List.of("消息发送事件");
            case "notice" -> List.of("通知事件");
            case "request" -> List.of("请求事件");
            case "meta_event" -> List.of("元事件");
            default -> List.of("任意事件");
        };
    }

    private void addScheduledEvent() {
        List<FieldMetadata> fields = new ArrayList<>();
        fields.add(FieldMetadata.builder()
                .fieldName("cronExpression")
                .fieldType("String")
                .description("Cron 表达式，最短执行间隔 5 分钟")
                .order(0)
                .required(true)
                .example("60")
                .inherited(false)
                .build());
        EventMetadata.EntityMetadata entityInfo = EventMetadata.EntityMetadata.builder()
                .entityName("ScheduledEvent")
                .fields(fields)
                .build();
        eventMetadataList.add(EventMetadata.builder()
                .eventType("scheduledEvent")
                .eventName("定时触发")
                .description("按照设定的时间间隔自动触发工作流")
                .order(-1)
                .categories(Collections.singletonList("系统事件"))
                .categoryOrders(Collections.singletonList(-1))
                .entityInfo(entityInfo)
                .build());
    }
}
