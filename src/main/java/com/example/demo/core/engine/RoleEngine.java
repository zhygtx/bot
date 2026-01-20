package com.example.demo.core.engine;

import com.example.demo.core.manager.RoleManager;
import com.example.demo.pojo.event.Event;
import com.example.demo.pojo.event.Msg;
import com.example.demo.pojo.task.Action;
import com.example.demo.pojo.task.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;

@Slf4j
@Component
public class RoleEngine implements RoleManager {

    /**
     * 根据作用域中获取到的具体规则获取动作
     * @param roles 作用域中获取到的具体规则
     * @param msgObj 消息对象
     * @return 获取到的需要执行的动作
     */
    @Override
    public List<Action> getActions(List<Role> roles, Object msgObj){
        log.debug("开始获取动作，初始规则数量: {}", roles.size());
        // 获取消息对象传导为父对象
        Event event = (Event) msgObj;
        log.debug("消息类型: {}", event.getEventType());
        // 获取作用域中获取到的具体规则，键值为规则ID，值为动作列表
        List<Action> actions = new ArrayList<>();

        // 过滤掉动作为空的类型
        int initialSize = roles.size();
        roles.removeIf(role ->
                role.getAction() == null || role.getAction().isEmpty());
        log.debug("过滤空动作后规则数量: {} (移除了 {} 个)", roles.size(), initialSize - roles.size());

        // 过滤掉暂未实现的消息类型
        initialSize = roles.size();
        roles.removeIf(role ->
                !event.getEventType().toString().contains(role.getMatchMode().toString()));
        log.debug("过滤不匹配消息类型后规则数量: {} (移除了 {} 个)", roles.size(), initialSize - roles.size());

        for (Role role : roles){
            log.debug("处理规则: {}, 匹配模式: {}", role.getId(), role.getMatchMode());
            switch (role.getMatchMode()){
                case text :
                    Msg msg = (Msg) msgObj;
                    if (msg.getType().contains("text")){
                        matchText(role, msg, actions);
                    }
                    break;
                case image :
                    break;
                case GroupIncrease :
                case GroupDecrease :
                    actions.addAll(role.getAction());
                    break;
                default:
                    log.debug("暂不支持的匹配模式: {}", role.getMatchMode());
                    break;
            }
        }
        log.debug("获取动作完成，生成 {} 个规则的动作", actions.size());
        return actions;
    }


    /**
     * 匹配文本消息
     * @param role 角色
     * @param msg 消息对象
     * @param actions 动作列表
     */
    private void matchText(Role role, Msg msg , List<Action> actions){
        log.debug("匹配文本消息，规则ID: {}, 正则表达式: {}", role.getId(), role.getPattern().pattern());
        
        Integer firstTextType = msg.getType().indexOf("text");
        String firstText = msg.getContent().get(firstTextType).get("text").toString();
        log.debug("待匹配文本: {}", firstText);

        // 创建一个Matcher对象
        Matcher matcher = role.getPattern().matcher(firstText);
        // 判断是否匹配
        if (matcher.matches()){
            log.debug("文本匹配成功，规则ID: {}", role.getId());
            // 获取动作，并按照执行顺序进行排序
            List<Action> roleActions = role.getAction().stream()
                    .sorted(Comparator.comparingInt(Action::getSeq))
                    .toList();
            log.debug("规则 {} 包含 {} 个动作，已按执行顺序排序", role.getId(), roleActions.size());
            
            // 判断是否需要提取文本
            if (role.isExtract() && role.getExtractPosition() != null && !role.getExtractPosition().isEmpty()) {
                log.debug("规则 {} 需要提取文本，提取位置: {}", role.getId(), role.getExtractPosition());
                // 获取动作的提取位置
                List<Integer> position = role.getExtractPosition().stream().toList();
                // 创建一个所提取的文本的列表
                List<String> extractText = new ArrayList<>();
                // 遍历提取位置
                for (Integer integer : position) {
                    // 判断提取位置是否有效
                    if (integer >= 0 && integer <= matcher.groupCount()) {
                        // 设置动作的提取文本
                        String extracted = matcher.group(integer);
                        extractText.add(extracted);
                        log.debug("提取位置 {} 的文本: {}", integer, extracted);
                    } else {
                        log.debug("提取位置 {} 无效，跳过", integer);
                    }
                }
                // 设置动作的提取文本
                roleActions.forEach(action -> action.setExtractText(extractText));
                log.debug("设置所有动作的提取文本: {}", extractText);
            }

            actions.addAll(roleActions);
            log.debug("添加动作到结果，规则ID: {}, 动作数量: {}", role.getId(), roleActions.size());
        } else {
            log.debug("文本匹配失败，规则ID: {}", role.getId());
        }
    }
}