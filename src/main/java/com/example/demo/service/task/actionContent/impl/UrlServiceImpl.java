package com.example.demo.service.task.actionContent.impl;

import com.example.demo.mapper.task.actionContent.UrlMapper;
import com.example.demo.pojo.task.actionContent.Url;
import com.example.demo.service.task.actionContent.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UrlServiceImpl implements UrlService {

    private final UrlMapper urlMapper;

    @Autowired
    public UrlServiceImpl(UrlMapper urlMapper) {
        this.urlMapper = urlMapper;
    }

    @Override
    public Url getUrl(String id) {
        List<Map<String, String>> params = urlMapper.getParamsById(id);
        Url url = urlMapper.getUrlById(id);
        for (Map<String, String> param : params){
            url.getParams().put(param.get("params_key"), param.get("params_value"));
        }
        return url;
    }

    @Override
    public List<Url> getAllUrls() {
        List<Url> urls = urlMapper.getAllUrls();
        for (Url url : urls) {
            loadUrlParams(url);
        }
        return urls;
    }

    @Override
    public List<Url> getUrlsByUserId(String userId) {
        List<Url> urls = urlMapper.selectByUserId(userId);
        for (Url url : urls) {
            loadUrlParams(url);
        }
        return urls;
    }

    @Override
    public Url addUrl(Url url) {
        url.setId(UUID.randomUUID().toString());
        // 插入URL
        urlMapper.insert(url);
        // 插入URL参数
        saveUrlParams(url);
        return url;
    }

    @Override
    public Url updateUrl(Url url) {
        // 更新URL
        urlMapper.update(url);
        // 删除旧参数
        urlMapper.deleteUrlParamsByUrlId(url.getId());
        // 插入新参数
        saveUrlParams(url);
        return url;
    }

    @Override
    public int deleteUrlById(String id) {
        // 删除URL参数
        urlMapper.deleteUrlParamsByUrlId(id);
        // 删除URL
        return urlMapper.deleteById(id);
    }

    /**
     * 加载URL参数
     * @param url URL对象
     */
    private void loadUrlParams(Url url) {
        if (url == null) {
            return;
        }
        List<Map<String, String>> params = urlMapper.getParamsById(url.getId());
        for (Map<String, String> param : params) {
            url.getParams().put(param.get("params_key"), param.get("params_value"));
        }
    }

    /**
     * 保存URL参数
     * @param url URL对象
     */
    private void saveUrlParams(Url url) {
        if (url == null || url.getParams() == null) {
            return;
        }
        for (Map.Entry<String, String> entry : url.getParams().entrySet()) {
            urlMapper.insertUrlParam(url.getId(), entry.getKey(), entry.getValue());
        }
    }
}
