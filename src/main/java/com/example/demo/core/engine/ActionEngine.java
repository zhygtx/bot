package com.example.demo.core.engine;

import com.example.demo.core.engine.executor.ApiExecutor;
import com.example.demo.core.engine.executor.TemplateExecutor;
import com.example.demo.core.engine.executor.TextExecutor;
import com.example.demo.core.engine.executor.UrlExecutor;
import com.example.demo.core.manager.ActionManager;
import com.example.demo.pojo.event.GroupEvent;
import com.example.demo.pojo.event.PrivateMsg;
import com.example.demo.pojo.task.Action;
import com.example.demo.pojo.task.Receiver;
import com.mikuac.shiro.common.utils.MsgUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
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
     * @return <消息类型<群号/qq号，消息内容>>执行结果
     */
    @Override
    public Map<Receiver.ReceiverType,Map<Long,String>> executeActions(List<Action> actions, Object msg) {
        log.debug("开始执行动作，共 {} 个规则", actions.size());
        Map<Receiver.ReceiverType,Map<Long,String>> receivers = new HashMap<>();
        //遍历所有动作
        for (Action action : actions){
            String result = "";
            log.debug("执行动作: {}, 类型: {}", action.getId(), action.getActionType());
            //处理相关动作
            switch (action.getActionType()) {
                case text -> result = handleText(action, msg);
                case image -> log.debug("图片动作类型暂未实现");
                case api -> result = handleApi(action, msg);
                case url -> result = handleUrl(action, msg);
                case template -> result = handleTemplate(action, msg);
                default -> log.debug("未知动作类型: {}", action.getActionType());
            }
            //处理接收对象
            if (result != null){
                if (!action.getReceivers().isEmpty()){
                    log.debug("动作消息接收对象: {}", action.getReceivers());
                    for (Receiver receiver : action.getReceivers()){
                        if (receiver.getReceiverType() == Receiver.ReceiverType.Default){
                            if (msg instanceof PrivateMsg privateMsg){
                                receivers.put(Receiver.ReceiverType.Private, Map.of(privateMsg.getUserId(), result));
                            }else {
                                GroupEvent groupEvent = (GroupEvent) msg;
                                if(action.isNeedAt()){
                                    result += MsgUtils.builder()
                                            .at(groupEvent.getUserId())
                                            .build();
                                }
                                receivers.put(Receiver.ReceiverType.Group, Map.of(groupEvent.getGroupId(), result));
                            }
                        }else {
                            if (receiver.getReceiverType() == Receiver.ReceiverType.Private){
                                receivers.put(Receiver.ReceiverType.Private, Map.of(receiver.getReceiverUserQQ(), result));
                            }else {
                                if (action.isNeedAt() && receiver.getReceiverUserQQ()>0 &&receiver.getReceiverType() == Receiver.ReceiverType.Group){
                                    result += MsgUtils.builder()
                                            .at(receiver.getReceiverUserQQ())
                                            .build();
                                }
                                receivers.put(receiver.getReceiverType(), Map.of(receiver.getReceiverGroupQQ(), result));
                            }
                        }
                    }
                }else {
                    log.warn("动作{}没有接收对象", action);
                }
            }

        }
        return receivers;
    }







    /**
     * 处理文本动作
     * @param action 动作对象
     * @param msgObj 消息对象
     * @return 执行结果
     */
    private String handleText(Action action ,Object msgObj){
        log.debug("处理文本动作: {}, dataId: {}", action.getId(), action.getDataId());
        String result = textExecutor.getText(action, msgObj);
        log.debug("文本动作执行结果: {}", result);
        return result;
    }

    /**
     * 处理api动作
     * @param action 动作对象
     * @param msgObj 消息对象
     * @return 执行结果
     */
    private String handleApi(Action action ,Object msgObj){
        log.debug("处理API动作: {}, dataId: {}", action.getId(), action.getDataId());
        String result = apiExecutor.executeApi(action, msgObj);
        log.debug("API动作执行结果: {}", result);
        return result;
    }

    /**
     * 处理url动作
     * @param action 动作对象
     * @param msgObj 消息对象
     * @return 执行结果
     */
    private String handleUrl(Action action ,Object msgObj){
        log.debug("处理URL动作: {}, dataId: {}", action.getId(), action.getDataId());
        String result = urlExecutor.executeUrl(action, msgObj);
        log.debug("URL动作执行结果: {}", result);
        return result;
    }

    /**
     * 处理api模板动作
     * @param action 动作对象
     * @param msgObj 消息对象
     * @return 执行结果
     */
    private String handleTemplate(Action action ,Object msgObj){
        log.debug("处理模板动作: {}, dataId: {}", action.getId(), action.getDataId());
        String result = templateExecutor.executeTemplate(action, msgObj);
        log.debug("模板动作执行结果: {}", result);
        return result;
    }
}
