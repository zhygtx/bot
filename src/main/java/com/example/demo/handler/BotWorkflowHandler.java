package com.example.demo.handler;

import com.example.demo.pojo.entity.workflow.WorkflowInfo;
import com.example.demo.service.WorkflowCacheService;
import com.example.demo.util.WorkflowUtil;
import com.github.zhygtx.napcat.event.BaseEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * BOT工作流处理器，用于处理BOT事件触发工作流的逻辑
 */
@Component
@Slf4j
public class BotWorkflowHandler {

    private final WorkflowCacheService workflowCacheService;
    private final WorkflowUtil workflowUtil;

    /**
     * 使用 WeakHashMap 防止同一事件对象因 SDK 继承分发机制被多次回调时重复处理。
     * Key: 事件对象（弱引用），Value: 标记是否已处理（纯标记，value 无需删除）。
     * 事件对象被 GC 后对应条目自动清理，无内存泄漏风险。
     */
    private final Map<BaseEvent, Boolean> processedEvents =
            Collections.synchronizedMap(new WeakHashMap<>());

    public BotWorkflowHandler(WorkflowCacheService workflowCacheService, WorkflowUtil workflowUtil) {
        this.workflowCacheService = workflowCacheService;
        this.workflowUtil = workflowUtil;
    }

    /**
     * 处理BOT事件，执行相关工作流。
     * <p>
     * 沿事件类继承链向上查找，自动匹配所有层级的工作流。
     * 例如 PrivateGroupMessageEvent 触发时依次查找：
     * PrivateGroupMessageEvent → PrivateMessageEvent → MessageEvent → BaseEvent，
     * 从而让"任意事件"（BaseEvent）配置的工作流被所有子类事件触发。
     * <p>
     * 同时利用事件对象本身的 WeakHashMap 标记，
     * 避免因 SDK 分发机制（onXxx → onParent → onAnyEvent 多次回调同一事件对象）
     * 导致的重复执行。同一条继承链内，同一工作流 ID 也通过 seenIds 去重。
     *
     * @param botQQ BOT QQ号
     * @param event BOT事件对象
     */
    public void handleBotEvent(Long botQQ, BaseEvent event) {
        // 此事件对象已被处理过（因继承分发多次回调），跳过
        if (processedEvents.putIfAbsent(event, true) != null) {
            return;
        }

        try {
            String runtimeType = event.getClass().getSimpleName();
            log.info("处理BOT事件: botQQ={}, eventType={}", botQQ, runtimeType);

            Set<String> seenIds = new HashSet<>();
            Class<?> clazz = event.getClass();

            // 沿继承链向上查找直到 BaseEvent
            while (clazz != null && BaseEvent.class.isAssignableFrom(clazz)) {
                List<WorkflowInfo> workflows =
                        workflowCacheService.getWorkflowsByBotEvent(botQQ, clazz.getSimpleName());
                for (WorkflowInfo wf : workflows) {
                    if (seenIds.add(wf.getId())) {
                        executeWorkflow(wf, event);
                    }
                }
                clazz = clazz.getSuperclass();
            }

            if (seenIds.isEmpty()) {
                log.debug("未找到与BOT事件 {}:{} 相关的工作流", botQQ, runtimeType);
            } else {
                log.info("BOT事件 {}:{} 执行了 {} 个工作流: {}",
                        botQQ, runtimeType, seenIds.size(), seenIds);
            }
        } catch (Exception e) {
            log.error("处理BOT事件异常", e);
        }
    }

    /**
     * 处理BOT事件，执行相关工作流
     * @param botQQ BOT QQ号
     * @param eventType 事件类型
     * @param botEventData BOT事件数据
     */
    public void handleBotEvent(Long botQQ, String eventType, Object botEventData) {
        try {
            log.info("处理BOT事件: botQQ={}, eventType={}", botQQ, eventType);
            
            // 从本地内存缓存获取相关工作流
            List<WorkflowInfo> workflows = workflowCacheService.getWorkflowsByBotEvent(botQQ, eventType);
            
            if (workflows.isEmpty()) {
                log.info("未找到与BOT事件 {}:{} 相关的工作流", botQQ, eventType);
                return;
            }
            
            // 执行每个工作流
            for (WorkflowInfo workflow : workflows) {
                executeWorkflow(workflow, botEventData);
            }
        } catch (Exception e) {
            log.error("处理BOT事件异常", e);
        }
    }

    /**
     * 执行工作流
     * @param workflowInfo 工作流信息
     * @param botEventData BOT事件数据
     */
    private void executeWorkflow(WorkflowInfo workflowInfo, Object botEventData) {
        try {
            workflowUtil.executeWorkflow(workflowInfo, "botEvent", botEventData);
        } catch (Exception e) {
            log.error("执行工作流异常", e);
        }
    }
}
