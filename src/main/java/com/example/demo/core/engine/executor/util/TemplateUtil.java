package com.example.demo.core.engine.executor.util;

import com.example.demo.pojo.Result;
import com.example.demo.pojo.task.actionContent.Template;
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

@Component
public class TemplateUtil {

    private final FissureService fissureService;

    @Autowired
    public TemplateUtil(FissureService fissureService) {
        this.fissureService = fissureService;
    }

    /**
     * 获取战区裂隙信息
     * @param key 搜索关键字
     * @param template 模板对象
     * @return 裂隙信息
     */
    public String getWarframeFissure(String key, Template template){
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
