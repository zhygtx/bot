package com.example.demo.service.task.actionContent;

import com.example.demo.pojo.task.actionContent.Url;

public interface UrlService {

    /**
     * 通过id查询Url
     * @param id url的id
     * @return Url
     */
    Url getUrl(String id);

}
