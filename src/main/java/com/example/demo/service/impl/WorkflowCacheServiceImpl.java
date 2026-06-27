package com.example.demo.service.impl;

import com.example.demo.mapper.workflow.WorkflowInfoMapper;
import com.example.demo.pojo.entity.workflow.Node;
import com.example.demo.pojo.entity.workflow.WorkflowInfo;
import com.example.demo.service.WorkflowCacheService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工作流缓存服务实现。
 * <p>
 * Bot 事件工作流索引使用本地 ConcurrentHashMap 缓存，O(1) 纯内存查找。
 * 定时任务部分保留 Redis ZSet 做时间调度。
 * 启动时从数据库全量加载所有启用的工作流到本地缓存。
 */
@Service
@Slf4j
public class WorkflowCacheServiceImpl implements WorkflowCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final WorkflowInfoMapper workflowInfoMapper;

    // ==================== 本地缓存 ====================
    // botQQ → eventType → Set<WorkflowInfo>
    private final Map<Long, Map<String, Set<WorkflowInfo>>> botEventCache = new ConcurrentHashMap<>();

    public WorkflowCacheServiceImpl(RedisTemplate<String, Object> redisTemplate,
                                     WorkflowInfoMapper workflowInfoMapper) {
        this.redisTemplate = redisTemplate;
        this.workflowInfoMapper = workflowInfoMapper;
    }

    @PostConstruct
    public void init() {
        reloadCache();
        initScheduledTasks();
    }

    // ==================== Bot 事件索引（本地缓存） ====================

    @Override
    public void addWorkflowToCache(WorkflowInfo wf) {
        if (wf == null || wf.getNodes() == null) return;
        Node botEventNode = findBotEventNode(wf.getNodes());
        if (botEventNode == null) return;
        Long botQQ = botEventNode.getBotQQ();
        String eventType = botEventNode.getEventType();
        if (botQQ == null || eventType == null) return;

        botEventCache
                .computeIfAbsent(botQQ, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(eventType, k -> ConcurrentHashMap.newKeySet())
                .add(wf);
    }

    @Override
    public void removeWorkflowFromCache(String workflowId) {
        for (Map<String, Set<WorkflowInfo>> eventMap : botEventCache.values()) {
            for (Set<WorkflowInfo> workflows : eventMap.values()) {
                workflows.removeIf(wf -> workflowId.equals(wf.getId()));
            }
        }
    }

    @Override
    public List<WorkflowInfo> getWorkflowsByBotEvent(Long botQQ, String eventType) {
        Map<String, Set<WorkflowInfo>> eventMap = botEventCache.get(botQQ);
        if (eventMap == null) {
            return Collections.emptyList();
        }
        Set<WorkflowInfo> workflows = eventMap.get(eventType);
        if (workflows == null || workflows.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(workflows);
    }

    @Override
    public void reloadCache() {
        botEventCache.clear();
        List<WorkflowInfo> allEnabled = workflowInfoMapper.selectAllEnabledWithBotEvent();
        for (WorkflowInfo wf : allEnabled) {
            addWorkflowToCache(wf);
        }
        log.info("本地缓存加载完成，共 {} 个工作流，{} 个 bot",
                allEnabled.size(), botEventCache.size());
    }

    private Node findBotEventNode(List<Node> nodes) {
        for (Node node : nodes) {
            if (Node.NodeType.botEvent.equals(node.getNodeType())
                    && !"scheduledEvent".equals(node.getEventType())) {
                return node;
            }
        }
        return null;
    }

    // ==================== 定时任务（保留 Redis ZSet） ====================

    @Override
    public void addScheduledTask(WorkflowInfo workflowInfo) {
        if (workflowInfo == null || workflowInfo.getNodes() == null) return;
        Node scheduledNode = findScheduledNode(workflowInfo.getNodes());
        if (scheduledNode == null || scheduledNode.getScheduledTime() == null) return;

        removeScheduledTask(workflowInfo.getId());
        long nextExecutionTime = System.currentTimeMillis()
                + (scheduledNode.getScheduledTime() * 1000L);
        redisTemplate.opsForZSet().add("scheduled_tasks", workflowInfo, nextExecutionTime);
        log.info("工作流 {} 已添加到定时任务，下次执行时间：{}", workflowInfo.getId(), nextExecutionTime);
    }

    @Override
    public void removeScheduledTask(String workflowId) {
        Set<Object> tasks = redisTemplate.opsForZSet().range("scheduled_tasks", 0, -1);
        if (tasks != null) {
            for (Object task : tasks) {
                if (task instanceof WorkflowInfo wf
                        && workflowId.equals(wf.getId())) {
                    redisTemplate.opsForZSet().remove("scheduled_tasks", task);
                    log.info("工作流 {} 已从定时任务中移除", workflowId);
                    break;
                }
            }
        }
    }

    @Override
    public List<WorkflowInfo> getScheduledTasks() {
        List<WorkflowInfo> tasks = new ArrayList<>();
        Set<Object> taskObjects = redisTemplate.opsForZSet().range("scheduled_tasks", 0, -1);
        if (taskObjects != null) {
            for (Object obj : taskObjects) {
                if (obj instanceof WorkflowInfo wf) {
                    tasks.add(wf);
                }
            }
        }
        log.info("获取到 {} 个定时任务", tasks.size());
        return tasks;
    }

    private Node findScheduledNode(List<Node> nodes) {
        for (Node node : nodes) {
            if (Node.NodeType.botEvent.equals(node.getNodeType())
                    && "scheduledEvent".equals(node.getEventType())) {
                return node;
            }
        }
        return null;
    }

    @Override
    public void removeWorkflowsByPluginId(String pluginId) {
        List<String> workflowIds = workflowInfoMapper.selectIdsByPluginId(pluginId);
        if (workflowIds == null || workflowIds.isEmpty()) {
            log.info("没有工作流使用插件 {}，跳过缓存清理", pluginId);
            return;
        }
        for (String id : workflowIds) {
            removeWorkflowFromCache(id);
            removeScheduledTask(id);
        }
        log.info("已移除插件 {} 相关的 {} 个工作流缓存（包含 Bot 事件和定时任务）",
                pluginId, workflowIds.size());
    }

    private void initScheduledTasks() {
        List<WorkflowInfo> tasks = workflowInfoMapper.selectAllScheduledTask();
        for (WorkflowInfo wf : tasks) {
            addScheduledTask(wf);
        }
        log.info("定时任务初始化完成，共 {} 个", tasks.size());
    }
}
