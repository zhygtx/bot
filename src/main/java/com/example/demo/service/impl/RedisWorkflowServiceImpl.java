package com.example.demo.service.impl;

import com.example.demo.pojo.workflow.Node;
import com.example.demo.pojo.workflow.WorkflowInfo;
import com.example.demo.service.RedisWorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Redis工作流服务类，用于维护工作流的Redis数据
 */
@Service
@Slf4j
public class RedisWorkflowServiceImpl implements RedisWorkflowService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisWorkflowServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 为工作流添加Redis数据
     * @param workflowInfo 工作流信息
     */
    public void addWorkflowToRedis(WorkflowInfo workflowInfo) {
        if (workflowInfo == null || workflowInfo.getNodes() == null) {
            log.warn("工作流信息为空，跳过Redis添加");
            return;
        }

        // 查找BOT事件节点
        Node botEventNode = findBotEventNode(workflowInfo.getNodes());
        if (botEventNode == null) {
            log.info("工作流 {} 中无BOT事件节点，跳过Redis添加", workflowInfo.getId());
            return;
        }

        // 生成Redis键并添加数据
        String redisKey = generateRedisKey(botEventNode, workflowInfo.getId());
        if (redisKey != null) {
            redisTemplate.opsForValue().set(redisKey, workflowInfo);
            log.info("工作流 {} 已添加到Redis，键：{}", workflowInfo.getId(), redisKey);
        }
    }

    /**
     * 从Redis中删除工作流数据
     * @param workflowId 工作流ID
     */
    public void removeWorkflowFromRedis(String workflowId) {
        // 查找所有包含该工作流ID的Redis键
        String pattern = "bot:*:event:*:workflow:" + workflowId;
        Set<String> keys = redisTemplate.keys(pattern);

        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("工作流 {} 已从Redis中删除，删除了 {} 个键", workflowId, keys.size());
        } else {
            log.info("工作流 {} 在Redis中不存在", workflowId);
        }
    }

    /**
     * 根据BOT事件获取相关工作流
     * @param botQQ BOT QQ号
     * @param eventType 事件类型
     * @return 工作流列表
     */
    public List<WorkflowInfo> getWorkflowsByBotEvent(Long botQQ, String eventType) {
        String pattern = "bot:" + botQQ + ":event:" + eventType + ":workflow:*";
        Set<String> keys = redisTemplate.keys(pattern);

        List<WorkflowInfo> workflows = new ArrayList<>();
        for (String key : keys) {
            WorkflowInfo workflow = (WorkflowInfo) redisTemplate.opsForValue().get(key);
            if (workflow != null) {
                workflows.add(workflow);
            }
        }

        log.info("根据BOT事件 {}:{} 找到 {} 个工作流", botQQ, eventType, workflows.size());
        return workflows;
    }

    /**
     * 查找工作流中的BOT事件节点
     * @param nodes 节点列表
     * @return BOT事件节点
     */
    private Node findBotEventNode(List<Node> nodes) {
        for (Node node : nodes) {
            if (Node.NodeType.botEvent.equals(node.getNodeType())) {
                return node;
            }
        }
        return null;
    }

    /**
     * 生成Redis键
     * 格式：bot:{botQQ}:event:{eventType}:workflow:{workflowId}
     * @param botEventNode BOT事件节点
     * @param workflowId 工作流ID
     * @return Redis键
     */
    private String generateRedisKey(Node botEventNode, String workflowId) {
        if (botEventNode == null || botEventNode.getBotQQ() == null || botEventNode.getEventType() == null) {
            log.warn("BOT事件节点信息不完整，无法生成Redis键");
            return null;
        }

        return "bot:" + botEventNode.getBotQQ() + ":event:" + botEventNode.getEventType() + ":workflow:" + workflowId;
    }

    /**
     * 添加定时任务到Redis
     * @param workflowInfo 工作流信息
     */
    @Override
    public void addScheduledTask(WorkflowInfo workflowInfo) {
        if (workflowInfo == null || workflowInfo.getNodes() == null) {
            log.warn("工作流信息为空，跳过定时任务添加");
            return;
        }

        // 查找定时节点
        Node scheduledNode = findScheduledNode(workflowInfo.getNodes());
        if (scheduledNode == null || scheduledNode.getScheduledTime() == null) {
            log.info("工作流 {} 中无定时节点或定时时间未设置，跳过定时任务添加", workflowInfo.getId());
            return;
        }

        // 计算下次执行时间戳
        long nextExecutionTime = System.currentTimeMillis() + (scheduledNode.getScheduledTime() * 1000);
        // 添加到Redis的Sorted Set
        redisTemplate.opsForZSet().add("scheduled_tasks", workflowInfo, nextExecutionTime);
        log.info("工作流 {} 已添加到定时任务，下次执行时间：{}", workflowInfo.getId(), nextExecutionTime);
    }

    /**
     * 从Redis中移除定时任务
     * @param workflowId 工作流ID
     */
    @Override
    public void removeScheduledTask(String workflowId) {
        // 从Redis的Sorted Set中移除定时任务
        Set<Object> tasks = redisTemplate.opsForZSet().range("scheduled_tasks", 0, -1);
        if (tasks != null) {
            for (Object task : tasks) {
                if (task instanceof WorkflowInfo workflow) {
                    if (workflowId.equals(workflow.getId())) {
                        redisTemplate.opsForZSet().remove("scheduled_tasks", task);
                        log.info("工作流 {} 已从定时任务中移除", workflowId);
                        break;
                    }
                }
            }
        }
    }

    /**
     * 获取所有定时任务
     * @return 工作流列表
     */
    @Override
    public List<WorkflowInfo> getScheduledTasks() {
        List<WorkflowInfo> tasks = new ArrayList<>();
        Set<Object> taskObjects = redisTemplate.opsForZSet().range("scheduled_tasks", 0, -1);
        if (taskObjects != null) {
            for (Object task : taskObjects) {
                if (task instanceof WorkflowInfo) {
                    tasks.add((WorkflowInfo) task);
                }
            }
        }
        log.info("获取到 {} 个定时任务", tasks.size());
        return tasks;
    }

    /**
     * 查找工作流中的定时节点
     * @param nodes 节点列表
     * @return 定时节点
     */
    private Node findScheduledNode(List<Node> nodes) {
        for (Node node : nodes) {
            if (Node.NodeType.botEvent.equals(node.getNodeType()) && "scheduledEvent".equals(node.getEventType())) {
                return node;
            }
        }
        return null;
    }
}
