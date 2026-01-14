package com.example.demo.core.engine;

import com.example.demo.core.manager.RoleManager;
import com.example.demo.pojo.msg.Msg;
import com.example.demo.pojo.task.Action;
import com.example.demo.pojo.task.Role;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RoleEngine implements RoleManager {

    /**
     * 根据作用域中获取到的具体规则获取动作
     * @param roles 作用域中获取到的具体规则
     * @param msgObj 消息对象
     * @return 获取到的需要执行的动作
     */
    @Override
    public Map<String, List<Action>> getActions(List<Role> roles, Object msgObj){
        // 获取消息对象传导为父对象
        Msg msg = (Msg) msgObj;
        // 获取作用域中获取到的具体规则，键值为规则ID，值为动作列表
        Map<String, List<Action>> actions = new HashMap<>();

        // 过滤掉动作为空的类型
        roles.removeIf(role ->
                role.getAction() == null || role.getAction().isEmpty());

        // 过滤掉暂未实现的消息类型
        roles.removeIf(role ->
                !msg.getType().contains(role.getMatchMode().toString()));

        for (Role role : roles){
            switch (role.getMatchMode()){
                case text :
                    if (msg.getType().contains("text")){
                        matchText(role, msg, actions);
                    }
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
     * @param msg 消息对象
     * @param actions 动作列表
     */
    private void matchText(Role role, Msg msg , Map<String, List<Action>> actions){
        Pattern pattern = role.getPattern();
        Integer firstTextType = msg.getType().indexOf("text");
        String firstText = msg.getContent().get(firstTextType).get("text").toString();

        // 创建一个Matcher对象
        Matcher matcher = pattern.matcher(firstText);
        // 判断是否匹配
        if (matcher.matches()){
            // 获取动作，并按照执行顺序进行排序
            List<Action> roleActions = role.getAction().stream()
                    .sorted(Comparator.comparingInt(Action::getSeq))
                    .toList();
            // 判断是否需要提取文本
            if (role.isExtract() && role.getExtractPosition() != null && !role.getExtractPosition().isEmpty()) {
                // 获取动作的提取位置
                List<Integer> position = role.getExtractPosition().stream().toList();
                // 创建一个所提取的文本的列表
                List<String> extractText = new ArrayList<>();
                // 遍历提取位置
                for (Integer integer : position) {
                    // 判断提取位置是否有效
                    if (integer >= 0 && integer <= matcher.groupCount()) {
                        // 设置动作的提取文本
                        extractText.add(matcher.group(integer));
                    }
                }
                // 设置动作的提取文本
                roleActions.forEach(action -> action.setExtractText(extractText));
            }

            actions.put(role.getId(), roleActions);
        }
    }
}