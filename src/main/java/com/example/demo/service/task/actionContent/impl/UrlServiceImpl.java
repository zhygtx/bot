package com.example.demo.service.task.actionContent.impl;

import com.example.demo.mapper.task.actionContent.UrlMapper;
import com.example.demo.pojo.task.actionContent.Url;
import com.example.demo.service.task.actionContent.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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
}
