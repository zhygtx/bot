package com.example.demo.core.engine.executor;

import com.example.demo.pojo.Result;
import com.example.demo.pojo.task.Action;
import com.example.demo.pojo.task.actionContent.Api;
import com.example.demo.pojo.task.actionContent.Template;
import com.example.demo.service.task.actionContent.ApiService;
import com.example.demo.service.task.actionContent.TemplateService;
import com.example.demo.utils.HTMLUtil;
import com.gbx.warframe.worldstate.pojo.Fissure;
import com.gbx.warframe.worldstate.service.FissureService;
import com.mikuac.shiro.common.utils.MsgUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模板执行器
 */
@Component
public class TemplateExecutor {

    private final ApiService apiService;
    private final FissureService fissureService;
    private final TemplateService templateService;

    @Autowired
    public TemplateExecutor(TemplateService templateService, FissureService fissureService, ApiService apiService) {
        this.templateService = templateService;
        this.fissureService = fissureService;
        this.apiService = apiService;
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
                return getWarframeFissure(action.getExtractText(),template);
            default:
                return "";
        }
    }

    /**
     * 获取战区裂隙信息
     * @param key 搜索关键字
     * @param template 模板对象
     * @return 裂隙信息
     */
    private String getWarframeFissure(String key,Template template){
        List<Fissure> fissures = fissureService.getFissures(key);
        if (fissures.isEmpty()){
            return "无相关裂隙";
        }
        //按裂隙等级排序
        Map<String, List<Fissure>> fissureMap = fissures.stream()
                .sorted(Comparator.comparingInt(Fissure::getModifierLevel))
                .collect(Collectors.groupingBy(
                        Fissure::getModifier,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        Result<String> result = HTMLUtil.objectToImage(fissureMap, template);

        if (result.getCode() == 1){
            return result.getMessage();
        }

        return MsgUtils.builder()
                .img("base64://"+ result.getData())
                .build();
    }

}
