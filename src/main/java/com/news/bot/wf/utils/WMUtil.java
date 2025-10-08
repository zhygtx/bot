package com.news.bot.wf.utils;

import com.news.bot.infrastructure.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class WMUtil {

    @Autowired
    private UrlService urlService;

    public String getAllItem(){
        String url = "https://api.warframe.market/v2/items";
        Map<String,String> headers = Map.of(
                "language", "zh-hans"
        );
        return urlService.fetchJson(url,headers);
    }

}
