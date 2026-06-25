package com.example.demo.handler.scanner;

import com.example.demo.annotation.BotEvent;
import com.example.demo.pojo.entity.metadata.EventMetadata;
import com.example.demo.pojo.entity.metadata.FieldMetadata;
import com.github.zhygtx.napcat.event.BaseEvent;
import com.github.zhygtx.napcat.event.OneBotEventListener;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * BOT 事件元数据扫描器。
 * <p>
 * 扫描所有实现了 {@link OneBotEventListener} 的 Spring Bean，
 * 提取标注了 {@link BotEvent} 的方法，并反射扫描对应事件类的字段。
 */
@Component
@Slf4j
public class BotEventScanner {

    @Getter
    private final List<EventMetadata> eventMetadataList = new ArrayList<>();

    private final ApplicationContext applicationContext;

    private final Set<String> scannedSignatures = new HashSet<>();

    public BotEventScanner(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        scanEvents();
    }

    /**
     * 扫描所有 BOT 事件。
     */
    private void scanEvents() {
        try {
            // 1. 添加内置定时触发事件
            addScheduledEvent();

            // 2. 扫描所有 OneBotEventListener Bean
            Map<String, OneBotEventListener> listenerBeans =
                    applicationContext.getBeansOfType(OneBotEventListener.class);

            for (OneBotEventListener bean : listenerBeans.values()) {
                Class<?> clazz = ClassUtils.getUserClass(bean);
                for (Method method : clazz.getDeclaredMethods()) {
                    BotEvent botEvent = method.getAnnotation(BotEvent.class);
                    if (botEvent == null) continue;

                    // 去重
                    String sig = clazz.getName() + "." + method.getName();
                    if (!scannedSignatures.add(sig)) continue;

                    // 从方法签名推断事件类（第二个参数）
                    Class<? extends BaseEvent> eventClass = inferEventClass(method);
                    if (eventClass == null) {
                        log.warn("无法推断事件类: {}.{}", clazz.getSimpleName(), method.getName());
                        continue;
                    }

                    // 反射扫描事件类字段
                    List<FieldMetadata> fields = scanEventClassFields(eventClass);

                    // 自动推导 eventType 和 entityName
                    String eventType = deriveEventType(eventClass);
                    String entityName = eventClass.getSimpleName();

                    EventMetadata.EntityMetadata entityInfo = EventMetadata.EntityMetadata.builder()
                            .entityName(entityName)
                            .fields(fields)
                            .build();

                    EventMetadata metadata = EventMetadata.builder()
                            .eventType(eventType)
                            .eventName(botEvent.name())
                            .description(botEvent.description())
                            .order(botEvent.order())
                            .categories(Arrays.asList(botEvent.categories()))
                            .categoryOrders(Arrays.stream(botEvent.categoryOrders()).boxed().collect(Collectors.toList()))
                            .entityInfo(entityInfo)
                            .build();

                    eventMetadataList.add(metadata);
                    log.info("扫描到BOT事件: {} ({})", botEvent.name(), eventType);
                }
            }

            eventMetadataList.sort(Comparator.comparingInt(EventMetadata::getOrder));
            log.info("BOT事件扫描完成，共扫描到 {} 个事件", eventMetadataList.size());

        } catch (Exception e) {
            log.error("扫描BOT事件失败", e);
        }
    }

    /**
     * 从方法签名推断事件类。
     * 要求第二个参数是 BaseEvent 的子类。
     */
    private Class<? extends BaseEvent> inferEventClass(Method method) {
        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length >= 2
                && paramTypes[0] == Long.class
                && BaseEvent.class.isAssignableFrom(paramTypes[1])) {
            @SuppressWarnings("unchecked")
            Class<? extends BaseEvent> eventClass = (Class<? extends BaseEvent>) paramTypes[1];
            return eventClass;
        }
        return null;
    }

    /**
     * 从事件类名自动推导 eventType。
     * <p>
     * 规则：去掉类名末尾的 "Event"，首字母转小写，其余保持 camelCase。
     * <ul>
     *   <li>{@code GroupMessageEvent} → {@code groupMessage}</li>
     *   <li>{@code PrivateFriendMessageEvent} → {@code privateFriendMessage}</li>
     *   <li>{@code GroupAdminSetNoticeEvent} → {@code groupAdminSetNotice}</li>
     *   <li>{@code HeartbeatMetaEvent} → {@code heartbeatMeta}</li>
     * </ul>
     */
    private String deriveEventType(Class<?> eventClass) {
        String simpleName = eventClass.getSimpleName();
        // 去掉末尾的 "Event"
        if (simpleName.endsWith("Event")) {
            simpleName = simpleName.substring(0, simpleName.length() - "Event".length());
        }
        // 首字母转小写
        if (!simpleName.isEmpty()) {
            simpleName = Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
        }
        return simpleName;
    }

    /**
     * 反射扫描事件类及其父类的所有字段，扁平化为 FieldMetadata 列表。
     * <p>
     * 沿继承链从 BaseEvent 向上到具体事件类，
     * 后出现的字段（子类）覆盖同名的先出现字段（父类）。
     */
    private List<FieldMetadata> scanEventClassFields(Class<?> eventClass) {
        Map<String, FieldMetadata> fieldMap = new LinkedHashMap<>(); // 保持插入顺序 + 去重

        // 收集继承链（父类在前）
        List<Class<?>> hierarchy = new ArrayList<>();
        Class<?> current = eventClass;
        while (current != null && current != Object.class) {
            hierarchy.add(0, current);
            current = current.getSuperclass();
        }

        int order = 0;
        for (Class<?> clazz : hierarchy) {
            boolean isInherited = clazz != eventClass;

            for (Field field : clazz.getDeclaredFields()) {
                if (FieldScanUtil.isScannableField(field)) continue;

                String jsonName = FieldScanUtil.getJsonFieldName(field);
                String desc = FieldScanUtil.resolveFieldDescription(clazz, field);

                FieldMetadata fm = FieldMetadata.builder()
                        .fieldName(jsonName)
                        .fieldType(FieldScanUtil.getSimpleTypeName(field.getType()))
                        .description(desc)
                        .order(order++)
                        .inherited(isInherited)
                        .required(false)
                        .example("")
                        .build();

                // 子类字段覆盖父类字段（按 JSON 名称去重）
                fieldMap.put(jsonName, fm);
            }
        }

        return new ArrayList<>(fieldMap.values());
    }

    /**
     * 添加内置的定时触发事件。
     */
    private void addScheduledEvent() {
        List<FieldMetadata> fields = new ArrayList<>();
        fields.add(FieldMetadata.builder()
                .fieldName("scheduledTime")
                .fieldType("Integer")
                .description("执行间隔时间（秒），最小60秒")
                .order(0)
                .required(true)
                .example("60")
                .inherited(false)
                .build());

        EventMetadata.EntityMetadata entityInfo = EventMetadata.EntityMetadata.builder()
                .entityName("ScheduledEvent")
                .fields(fields)
                .build();

        EventMetadata scheduledEvent = EventMetadata.builder()
                .eventType("scheduledEvent")
                .eventName("定时触发")
                .description("按照设定的时间间隔自动触发工作流")
                .order(-1)
                .categories(List.of("系统事件"))
                .categoryOrders(List.of(-1))
                .entityInfo(entityInfo)
                .build();

        eventMetadataList.add(scheduledEvent);
        log.info("内置事件: 定时触发");
    }

    /**
     * 根据事件类型获取事件元数据。
     */
    public EventMetadata getEventByType(String eventType) {
        return eventMetadataList.stream()
                .filter(e -> e.getEventType().equals(eventType))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取所有启用的事件（排除 scheduledEvent 等特殊事件）。
     */
    public List<EventMetadata> getEnabledEvents() {
        return eventMetadataList.stream()
                .filter(e -> e.getOrder() >= 0)
                .toList();
    }
}
