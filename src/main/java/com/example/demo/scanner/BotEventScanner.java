package com.example.demo.scanner;

import com.example.demo.annotation.BotEvent;
import com.example.demo.annotation.EventParam;
import com.example.demo.metadata.EventMetadata;
import com.example.demo.metadata.FieldMetadata;
import com.mikuac.shiro.annotation.common.Shiro;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;

@Component
@Slf4j
public class BotEventScanner {

    @Getter
    private final List<EventMetadata> eventMetadataList = new ArrayList<>();

    private final ApplicationContext applicationContext;

    private final Set<String> scannedMethods = new HashSet<>();

    public BotEventScanner(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        scanEvents();
    }

    /**
     * 扫描BOT事件
     */
    private void scanEvents() {
        try {
            Map<String, Object> shiroBeans = applicationContext.getBeansWithAnnotation(Shiro.class);

            for (Object bean : shiroBeans.values()) {
                Class<?> clazz = bean.getClass();

                for (Method method : clazz.getDeclaredMethods()) {
                    BotEvent botEvent = method.getAnnotation(BotEvent.class);

                    if (botEvent != null) {
                        String methodSignature = clazz.getName() + "." + method.getName();

                        if (!scannedMethods.contains(methodSignature)) {
                            EventMetadata metadata = extractEventMetadata(method, botEvent);
                            eventMetadataList.add(metadata);
                            scannedMethods.add(methodSignature);

                            log.info("扫描到BOT事件: {} ({}) - {}",
                                botEvent.name(),
                                botEvent.eventType(),
                                botEvent.description());
                        }
                    }
                }
            }

            eventMetadataList.sort(Comparator.comparingInt(EventMetadata::getOrder));
            log.info("BOT事件扫描完成，共扫描到 {} 个事件", eventMetadataList.size());

        } catch (Exception e) {
            log.error("扫描BOT事件失败", e);
        }
    }

    /**
     * 提取事件元数据
     * @param method 方法
     * @param botEvent 注解
     * @return 事件元数据
     */
    private EventMetadata extractEventMetadata(Method method, BotEvent botEvent) {
        List<FieldMetadata> fields = new ArrayList<>();

        EventParam[] eventParams = method.getAnnotationsByType(EventParam.class);
        Arrays.sort(eventParams, Comparator.comparingInt(EventParam::order));

        for (EventParam param : eventParams) {
            fields.add(FieldMetadata.builder()
                .fieldName(param.name())
                .fieldType(param.type())
                .description(param.description())
                .order(param.order())
                .required(param.required())
                .example(param.example())
                .inherited(false)
                .build());
        }

        EventMetadata.EntityMetadata entityInfo = EventMetadata.EntityMetadata.builder()
            .entityName(botEvent.entityName())
            .fields(fields)
            .build();

        return EventMetadata.builder()
            .eventType(botEvent.eventType())
            .eventName(botEvent.name())
            .description(botEvent.description())
            .order(botEvent.order())
            .entityInfo(entityInfo)
            .build();
    }

    /**
     * 根据事件类型获取事件元数据
     * @param eventType 事件类型
     * @return 事件元数据
     */
    public EventMetadata getEventByType(String eventType) {
        return eventMetadataList.stream()
            .filter(e -> e.getEventType().equals(eventType))
            .findFirst()
            .orElse(null);
    }

    /**
     * 获取所有启用的事件
     * @return 所有启用的事件
     */
    public List<EventMetadata> getEnabledEvents() {
        return eventMetadataList.stream()
            .filter(e -> e.getOrder() >= 0)
            .toList();
    }
}
