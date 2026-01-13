package com.example.demo.service.task.actionContent;

import com.example.demo.pojo.task.actionContent.Url;

import java.util.List;

public interface UrlService {

    /**
     * 通过id查询Url
     * @param id url的id
     * @return Url
     */
    Url getUrl(String id);

    /**
     * 获取所有URL
     * @return URL列表
     */
    List<Url> getAllUrls();

    /**
     * 根据用户ID获取URL列表
     * @param userId 用户ID
     * @return URL列表
     */
    List<Url> getUrlsByUserId(String userId);

    /**
     * 添加URL
     * @param url URL
     * @return URL
     */
    Url addUrl(Url url);

    /**
     * 更新URL
     * @param url URL
     * @return URL
     */
    Url updateUrl(Url url);

    /**
     * 删除URL
     * @param id URL ID
     * @return 删除数量
     */
    int deleteUrlById(String id);

}
