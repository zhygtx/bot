package com.example.demo.core.engine;

import com.example.demo.core.engine.executor.ApiExecutor;
import com.example.demo.core.engine.executor.TextExecutor;
import com.example.demo.core.engine.executor.UrlExecutor;
import com.example.demo.core.manager.ActionManager;
import com.example.demo.pojo.msg.GroupMsg;
import com.example.demo.pojo.task.Action;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ActionEngine implements ActionManager {

    private final UrlExecutor urlExecutor;
    private final TextExecutor textExecutor;
    private final ApiExecutor apiExecutor;

    @Autowired
    public ActionEngine(TextExecutor textExecutor, ApiExecutor apiExecutor, UrlExecutor urlExecutor) {
        this.textExecutor = textExecutor;
        this.apiExecutor = apiExecutor;
        this.urlExecutor = urlExecutor;
    }

    /**
     * 执行动作
     * @param actions 动作列表
     * @param groupMsg 群消息对象
     * @return 执行结果
     */
    @Override
    public List<String> executeActions(Map<String,List<Action>> actions, GroupMsg groupMsg) {

        List<String> result = new ArrayList<>();
        //遍历所有动作
        for (Map.Entry<String, List<Action>> entry : actions.entrySet()){
            List<String> text = new ArrayList<>();//存储单个规则的返回消息
            //遍历单个规则中的所有动作
            for (Action action : entry.getValue()){
                //异常处理
                try {
                    //处理相关动作
                    switch (action.getActionType()){
                        case text:
                            handleText(action, text, groupMsg);
                            break;
                        case image:
                            //图片处理逻辑
                            break;
                        case api:
                            handleApi(action, text, groupMsg);
                            break;
                        case url:
                            handleUrl(action, text, groupMsg);
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    log.warn("执行动作失败: {}", action.getId(), e);
                }
            }
            if (!text.isEmpty()){
                result.addAll(text);
            }
        }
        return result;
    }

    /**
     * 追加文本结果
     * @param action 动作对象
     * @param text 存储返回消息的列表
     * @param msg 返回消息
     */
    private void appendTextResult(Action action, List<String> text, String msg) {
        if (msg == null){
            return;
        }
        if (text.isEmpty()){
            text.add(msg);
            return;
        }
        if (action.isConcat()){
            text.set(text.size() - 1, text.get(text.size() - 1) + msg);
        }else {
            text.add(msg);
        }
    }

    /**
     * 处理文本动作
     * @param action 动作对象
     * @param text 存储返回消息的列表
     * @param groupMsg 群消息对象
     */
    private void handleText(Action action ,List<String> text, GroupMsg groupMsg){
        String msg = textExecutor.getText(action, groupMsg);
        appendTextResult(action, text, msg);
    }

    /**
     * 处理api动作
     * @param action 动作对象
     * @param text 存储返回消息的列表
     * @param groupMsg 群消息对象
     */
    private void handleApi(Action action ,List<String> text, GroupMsg groupMsg){
        String msg = apiExecutor.executeApi(action, groupMsg);
        appendTextResult(action, text, msg);
    }

    private void handleUrl(Action action ,List<String> text, GroupMsg groupMsg){
        String msg = urlExecutor.executeUrl(action, groupMsg);
        appendTextResult(action, text, msg);
    }
}
