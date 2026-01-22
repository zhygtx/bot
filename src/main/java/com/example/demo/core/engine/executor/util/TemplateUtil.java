package com.example.demo.core.engine.executor.util;

import com.example.demo.pojo.Result;
import com.example.demo.pojo.task.actionContent.Template;
import com.example.demo.utils.HTMLUtil;
import com.gbx.warframe.worldstate.pojo.Fissure;
import com.gbx.warframe.worldstate.service.FissureService;
import com.mikuac.shiro.common.utils.MsgUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
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
        log.debug("开始获取战区裂隙信息，关键字: {}, 模板ID: {}", key, template.getId());
        
        List<Fissure> fissures = fissureService.getFissures(key);
        log.debug("获取到裂隙数量: {}", fissures.size());
        
        if (fissures.isEmpty()){
            log.debug("无相关裂隙，返回提示信息");
            return "无相关裂隙";
        }
        
        //按裂隙等级排序
        log.debug("开始按裂隙等级排序");
        Map<String, List<Fissure>> fissureMap = fissures.stream()
                .sorted(Comparator.comparingInt(Fissure::getModifierLevel))
                .collect(Collectors.groupingBy(
                        Fissure::getModifier,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        log.debug("排序完成，裂隙分组数量: {}", fissureMap.size());

        log.debug("开始生成裂隙图像");
        Result<String> result = HTMLUtil.objectToImage(fissureMap, template);
        log.debug("图像生成结果，代码: {}, 消息: {}", result.getCode(), result.getMessage());

        if (result.getCode() == 1){
            log.debug("图像生成失败，返回错误信息: {}", result.getMessage());
            return result.getMessage();
        }

        String finalResult = MsgUtils.builder()
                .img("base64://"+ result.getData())
                .build();
        log.debug("图像生成成功，返回结果: {}", finalResult);
        return finalResult;
    }

}
