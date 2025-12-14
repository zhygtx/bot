package com.example.demo.core.engine;

import com.example.demo.core.manager.RoleManager;
import com.example.demo.pojo.msg.GroupMsg;
import com.example.demo.pojo.task.Action;
import com.example.demo.pojo.task.Role;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

@Component
public class RoleEngine implements RoleManager {

    /**
     * 根据作用域中获取到的具体规则获取动作
     * @param roles 作用域中获取到的具体规则
     * @param groupMsg 消息对象
     * @return 获取到的需要执行的动作
     */
    @Override
    public Map<String, List<Action>> getActions(Set<Role> roles, GroupMsg groupMsg){
        Map<String, List<Action>> actions = new HashMap<>();

        // 过滤掉动作为空的类型
        roles.removeIf(role ->
                role.getAction() == null || role.getAction().isEmpty());

        // 过滤掉暂未实现的消息类型
        roles.removeIf(role ->
                !groupMsg.getType().contains(role.getMatchMode().toString()));

        for (Role role : roles){
            switch (role.getMatchMode()){
                case text :
                    matchText(role, groupMsg, actions);
                    break;
                case image :
                default:
                    break;
            }
        }
        return actions;
    }


    /**
     * 匹配文本消息
     * @param role 角色
     * @param groupMsg 消息对象
     * @param actions 动作列表
     */
    private void matchText(Role role, GroupMsg groupMsg , Map<String, List<Action>> actions){
        Pattern pattern = role.getPattern();
        Integer firstTextType = groupMsg.getType().indexOf("text");
        String firstText = groupMsg.getContent().get(firstTextType).get("text").toString();

        if (pattern != null && pattern.matcher(firstText).matches()){
            // 获取动作，并按照执行顺序进行排序
            List<Action> roleActions = role.getAction().stream()
                    .sorted(Comparator.comparingInt(Action::getSeq))
                    .toList();

            if (role.isExtract()){
                String extractText = firstText.substring(role.getExtractPosition());
                // 设置提取的文本
                roleActions.forEach(action ->
                        action.setExtractText(extractText));
            }

            actions.put(role.getId(), roleActions);
        }
    }
}