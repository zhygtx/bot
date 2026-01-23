package com.example.demo.core.engine.executor.util;

import com.example.demo.pojo.Result;
import com.example.demo.pojo.task.actionContent.Template;
import com.example.demo.utils.HTMLUtil;
import com.mikuac.shiro.common.utils.MsgUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TemplateUtil {

    private final HTMLUtil htmlUtil;

    public TemplateUtil(HTMLUtil htmlUtil) {
        this.htmlUtil = htmlUtil;
    }

    /**
     * 获取战区裂隙信息
     * @param obj 对象
     * @param template 模板对象
     * @return 裂隙信息
     */
    public String objectToImage(Object obj,Template template){

        log.debug("开始生成裂隙图像");
        Result<String> result = htmlUtil.objectToImage(obj, template);
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
