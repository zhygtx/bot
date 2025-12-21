package com.example.demo.core.engine;

import com.example.demo.core.manager.RoleManager;
import com.example.demo.pojo.msg.Msg;
import com.example.demo.pojo.task.Action;
import com.example.demo.pojo.task.Role;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        if (matcher.matches()){
            // 获取动作，并按照执行顺序进行排序
            List<Action> roleActions = role.getAction().stream()
                    .sorted(Comparator.comparingInt(Action::getSeq))
                    .toList();

            if (role.isExtract() && role.getExtractPosition() != null) {
                int position = role.getExtractPosition();
                // 确保捕获组位置在有效范围内
                if (position >= 0 && position <= matcher.groupCount()) {
                    // 提取指定组的文本
                    String extractText = matcher.group(position);
                    // 设置所有动作中所提取的文本
                    roleActions.forEach(action -> action.setExtractText(extractText));
                }
            }

            actions.put(role.getId(), roleActions);
        }
    }
}