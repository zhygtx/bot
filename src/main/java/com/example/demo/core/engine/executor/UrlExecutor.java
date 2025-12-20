package com.example.demo.core.engine.executor;

import com.example.demo.pojo.msg.GroupMsg;
import com.example.demo.pojo.task.Action;
import com.example.demo.pojo.task.actionContent.Url;
import com.example.demo.service.task.actionContent.UrlService;
import com.example.demo.utils.UrlUtil;
import com.mikuac.shiro.common.utils.MsgUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UrlExecutor {

    private final UrlService urlService;

    @Autowired
    public UrlExecutor(UrlService urlService) {
        this.urlService = urlService;
    }

    public String executeUrl(Action action, GroupMsg groupMsg){
        String result;
        //获取URL信息并拼接URL参数
        Url urlInfo = urlService.getUrl(action.getDataId());
        StringBuilder url = new StringBuilder(urlInfo.getUrl());
        //拼接URL参数
        if (urlInfo.getParams() != null && !urlInfo.getParams().isEmpty()){
            url.append("?");
            for (String key : urlInfo.getParams().keySet()){
                url.append(key).append("=").append(urlInfo.getParams().get(key)).append("&");
            }
            //删除最后一个多余的"&"
            url = new StringBuilder(url.substring(0, url.length() - 1));
        }
        try{
            String imageUrl = UrlUtil.retrieveUrl(url.toString());
            result = MsgUtils.builder()
                    .img(imageUrl)
                    .build();
        } catch (Exception e){
            return "URL解析错误"+e.getMessage()+"\n";
        }
        return result;
    }
}
