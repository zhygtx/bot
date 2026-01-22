package com.example.demo.core.engine.executor;

import com.example.demo.pojo.event.Msg;
import com.example.demo.pojo.task.Action;
import com.example.demo.pojo.task.actionContent.Url;
import com.example.demo.service.task.actionContent.UrlService;
import com.example.demo.utils.StringTemplateUtil;
import com.example.demo.utils.UrlUtil;
import com.mikuac.shiro.common.utils.MsgUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UrlExecutor {

    private final UrlService urlService;
    private final StringTemplateUtil stringTemplateUtil;

    public UrlExecutor(UrlService urlService, StringTemplateUtil stringTemplateUtil) {
        this.urlService = urlService;
        this.stringTemplateUtil = stringTemplateUtil;
    }

    /**
     * 执行URL动作
     * @param action 动作对象
     * @param msg 群消息对象
     * @return 执行结果
     */
    public String executeUrl(Action action, Object msg){
        log.debug("开始执行URL动作，动作ID: {}, 数据ID: {}", action.getId(), action.getDataId());
        String result;
        
        //获取URL信息并拼接URL参数
        Url urlInfo = urlService.getUrl(action.getDataId());
        log.debug("获取到URL信息，基础URL: {}", urlInfo.getUrl());
        
        StringBuilder url = new StringBuilder(urlInfo.getUrl());
        //拼接URL参数
        if (urlInfo.getParams() != null && !urlInfo.getParams().isEmpty()){
            log.debug("开始拼接URL参数，参数数量: {}", urlInfo.getParams().size());
            url.append("?");
            for (String key : urlInfo.getParams().keySet()){
                String value = urlInfo.getParams().get(key);
                url.append(key).append("=").append(value).append("&");
                log.debug("添加参数: {}={}", key, value);
            }
            //删除最后一个多余的"&"
            url = new StringBuilder(url.substring(0, url.length() - 1));
            log.debug("参数拼接完成，完整URL: {}", url);
        }
        url = new StringBuilder(stringTemplateUtil.render(url.toString(), ((Msg) msg).getData(), action.getExtractText()));
        log.debug("URL渲染完成: {}", url);
        
        try{
            log.debug("开始解析URL: {}", url);
            String urlData = UrlUtil.retrieveUrl(url.toString());
            log.debug("URL解析成功，获取到数据: {}", urlData);

            result = "";
            if (urlData.startsWith("{")){//JSON数据
                result = MsgUtils.builder()
                        .json(urlData)
                        .build();
            }else if (urlData.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$")){//Base64数据
                result = MsgUtils.builder()
                        .img("base64://"+urlData)
                        .build();
            }else if (urlData.startsWith("http")){//HTTP数据
                // 检查是否为图片 URL
                result = MsgUtils.builder()
                        .img(urlData)
                        .build();
            }else if (urlData.startsWith("base64://")){//Base64数据
                result = MsgUtils.builder()
                        .img(urlData)
                        .build();
            }

        } catch (Exception e){
            log.error("URL解析错误: {}", e.getMessage(), e);
            return "URL解析错误"+e.getMessage()+"\n";
        }
        
        log.debug("URL动作执行完成，返回结果: {}", result);
        return result;
    }
}
