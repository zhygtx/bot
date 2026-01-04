package com.example.demo.core.engine.executor;

import com.example.demo.core.engine.executor.util.TemplateUtil;
import com.example.demo.pojo.task.Action;
import com.example.demo.pojo.task.actionContent.Api;
import com.example.demo.pojo.task.actionContent.Template;
import com.example.demo.service.task.actionContent.ApiService;
import com.example.demo.service.task.actionContent.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 模板执行器
 */
@Component
public class TemplateExecutor {

    private final ApiService apiService;
    private final TemplateService templateService;
    private final TemplateUtil templateUtil;

    @Autowired
    public TemplateExecutor(TemplateService templateService, ApiService apiService, TemplateUtil templateUtil) {
        this.templateService = templateService;
        this.apiService = apiService;
        this.templateUtil = templateUtil;
    }

    /**
     * 执行模板
     * @param action 动作对象
     * @param msg 消息对象
     * @return 执行结果
     */
    public String executeTemplate(Action action,Object msg) {
        Template template = templateService.getById(action.getDataId());
        String result = "";
        switch (template.getTemplateType()) {
            case text:
                //文本处理逻辑
                break;
            case url:
                //url处理逻辑
                break;
            case api:
                result = api(template, action);
            default:
                break;
        }
        return result;
    }

    /**
     * 执行api
     * @param template 模板对象
     * @param action 动作对象
     * @return 执行结果
     */
    private String api(Template template, Action action){
        Api api = apiService.getApi(template.getDataId());
        switch (api.getName()){
            case getWarframeFissure:
                return templateUtil.getWarframeFissure(action.getExtractText().get(0),template);
            default:
                return "";
        }
    }

}
