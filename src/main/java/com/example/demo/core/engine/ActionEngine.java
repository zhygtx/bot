package com.example.demo.core.engine;

import com.example.demo.core.engine.executor.ApiExecutor;
import com.example.demo.core.engine.executor.TemplateExecutor;
import com.example.demo.core.engine.executor.TextExecutor;
import com.example.demo.core.engine.executor.UrlExecutor;
import com.example.demo.core.manager.ActionManager;
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
    private final TemplateExecutor templateExecutor;

    @Autowired
    public ActionEngine(TextExecutor textExecutor, ApiExecutor apiExecutor, UrlExecutor urlExecutor, TemplateExecutor templateExecutor) {
        this.textExecutor = textExecutor;
        this.apiExecutor = apiExecutor;
        this.urlExecutor = urlExecutor;
        this.templateExecutor = templateExecutor;
    }

    /**
     * 执行动作
     * @param actions 动作列表
     * @param msg 群消息对象
     * @return 执行结果
     */
    @Override
    public List<String> executeActions(Map<String,List<Action>> actions, Object msg) {
        log.debug("开始执行动作，共 {} 个规则", actions.size());
        List<String> result = new ArrayList<>();
        //遍历所有动作
        for (Map.Entry<String, List<Action>> entry : actions.entrySet()){
            String ruleId = entry.getKey();
            List<Action> ruleActions = entry.getValue();
            log.debug("执行规则: {}, 包含 {} 个动作", ruleId, ruleActions.size());
            List<String> text = new ArrayList<>();//存储单个规则的返回消息
            //遍历单个规则中的所有动作
            for (Action action : ruleActions){
                //异常处理
                try {
                    log.debug("执行动作: {}, 类型: {}", action.getId(), action.getActionType());
                    //处理相关动作
                    switch (action.getActionType()){
                        case text:
                            handleText(action, text, msg);
                            break;
                        case image:
                            //图片处理逻辑
                            log.debug("图片动作类型暂未实现");
                            break;
                        case api:
                            handleApi(action, text, msg);
                            break;
                        case url:
                            handleUrl(action, text, msg);
                            break;
                        case template:
                            handleTemplate(action, text, msg);
                            break;
                        default:
                            log.debug("未知动作类型: {}", action.getActionType());
                            break;
                    }
                } catch (Exception e) {
                    log.warn("执行动作失败: {}", action.getId(), e);
                }
            }
            if (!text.isEmpty()){
                log.debug("规则 {} 执行完成，生成 {} 条消息", ruleId, text.size());
                result.addAll(text);
            }
        }
        log.debug("所有动作执行完成，共生成 {} 条结果", result.size());
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
     * @param msgObj 消息对象
     */
    private void handleText(Action action ,List<String> text, Object msgObj){
        log.debug("处理文本动作: {}, dataId: {}", action.getId(), action.getDataId());
        String msg = textExecutor.getText(action, msgObj);
        log.debug("文本动作执行结果: {}", msg);
        appendTextResult(action, text, msg);
    }

    /**
     * 处理api动作
     * @param action 动作对象
     * @param text 存储返回消息的列表
     * @param msgObj 消息对象
     */
    private void handleApi(Action action ,List<String> text, Object msgObj){
        log.debug("处理API动作: {}, dataId: {}", action.getId(), action.getDataId());
        String msg = apiExecutor.executeApi(action, msgObj);
        log.debug("API动作执行结果: {}", msg);
        appendTextResult(action, text, msg);
    }

    /**
     * 处理url动作
     * @param action 动作对象
     * @param text 存储返回消息的列表
     * @param msgObj 消息对象
     */
    private void handleUrl(Action action ,List<String> text, Object msgObj){
        log.debug("处理URL动作: {}, dataId: {}", action.getId(), action.getDataId());
        String msg = urlExecutor.executeUrl(action, msgObj);
        log.debug("URL动作执行结果: {}", msg);
        appendTextResult(action, text, msg);
    }

    /**
     * 处理api模板动作
     * @param action 动作对象
     * @param text 存储返回消息的列表
     * @param msgObj 消息对象
     */
    private void handleTemplate(Action action ,List<String> text, Object msgObj){
        log.debug("处理模板动作: {}, dataId: {}", action.getId(), action.getDataId());
        String msg = templateExecutor.executeTemplate(action, msgObj);
        log.debug("模板动作执行结果: {}", msg);
        appendTextResult(action, text, msg);
    }
}
