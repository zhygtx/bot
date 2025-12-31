package com.example.demo.service.task.actionContent.impl;

import com.example.demo.mapper.task.actionContent.TemplateMapper;
import com.example.demo.pojo.task.actionContent.Template;
import com.example.demo.service.task.actionContent.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 模板服务实现类
 */
@Service
public class TemplateServiceImpl implements TemplateService {

    private final TemplateMapper templateMapper;

    @Autowired
    public TemplateServiceImpl(TemplateMapper templateMapper) {
        this.templateMapper = templateMapper;
    }

    /**
     * 根据ID获取模板
     * @param id 模板ID
     * @return 模板实体
     */
    @Override
    public Template getById(String id){
        return templateMapper.getById(id);
    }

}
