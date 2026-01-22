package com.example.demo.core.engine.executor;

import com.example.demo.core.engine.executor.util.TemplateUtil;
import com.example.demo.pojo.event.Event;
import com.example.demo.pojo.task.Action;
import com.example.demo.pojo.task.actionContent.Api;
import com.example.demo.pojo.task.actionContent.Template;
import com.example.demo.service.task.actionContent.ApiService;
import com.example.demo.service.task.actionContent.TemplateService;
import com.example.demo.utils.StringTemplateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 模板执行器
 */
@Component
@Slf4j
public class TemplateExecutor {

    private final ApiService apiService;
    private final TemplateService templateService;
    private final TemplateUtil templateUtil;
    private final StringTemplateUtil stringTemplateUtil;

    public TemplateExecutor(TemplateService templateService, ApiService apiService, TemplateUtil templateUtil, StringTemplateUtil stringTemplateUtil) {
        this.templateService = templateService;
        this.apiService = apiService;
        this.templateUtil = templateUtil;
        this.stringTemplateUtil = stringTemplateUtil;
    }

    /**
     * 执行模板
     * @param action 动作对象
     * @param msg 消息对象
     * @return 执行结果
     */
    public String executeTemplate(Action action,Object msg) {
        log.debug("开始执行模板动作，动作ID: {}, 数据ID: {}", action.getId(), action.getDataId());
        
        Template template = templateService.getById(action.getDataId());
        log.debug("获取到模板，模板ID: {}, 类型: {}", template.getId(), template.getTemplateType());

        template.setContent(stringTemplateUtil.render(template.getContent(), ((Event) msg).getData(), action.getExtractText()));
        log.debug("模板内容渲染完成: {}", template.getContent());

        String result = "";
        switch (template.getTemplateType()) {
            case text:
                log.debug("模板所需数据类型为文本，执行文本处理逻辑");
                //文本处理逻辑
                break;
            case url:
                log.debug("模板所需数据类型为URL，执行URL处理逻辑");
                //url处理逻辑
                break;
            case api:
                log.debug("模板所需数据类型为API，执行API处理逻辑");
                result = api(template, action);
                break;
            default:
                log.debug("未知模板类型: {}", template.getTemplateType());
                break;
        }
        log.debug("模板执行完成，返回结果: {}", result);
        return result;
    }

    /**
     * 执行api
     * @param template 模板对象
     * @param action 动作对象
     * @return 执行结果
     */
    private String api(Template template, Action action){
        log.debug("开始执行模板API，模板ID: {}, 动作ID: {}", template.getId(), action.getId());
        
        Api api = apiService.getApi(template.getDataId());
        log.debug("获取到API，API名称: {}", api.getName());
        
        String result = "";
        switch (api.getName()){
            case getWarframeFissure:
                log.debug("执行获取战区裂隙API，提取文本: {}", action.getExtractText().get(0));
                result = templateUtil.getWarframeFissure(action.getExtractText().get(0),template);
                break;
            default:
                log.debug("未知API名称: {}", api.getName());
                break;
        }
        log.debug("模板API执行完成，返回结果: {}", result);
        return result;
    }

}
