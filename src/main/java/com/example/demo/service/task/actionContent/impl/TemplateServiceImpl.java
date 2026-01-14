package com.example.demo.service.task.actionContent.impl;

import com.example.demo.mapper.task.actionContent.TemplateMapper;
import com.example.demo.pojo.task.actionContent.Template;
import com.example.demo.service.task.actionContent.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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

    /**
     * 获取所有模板
     * @return 模板列表
     */
    @Override
    public List<Template> getAllTemplates() {
        return templateMapper.getAllTemplates();
    }

    /**
     * 根据ID获取模板
     * @param id 模板ID
     * @return 模板
     */
    @Override
    public Template getTemplateById(String id) {
        return templateMapper.selectById(id);
    }

    /**
     * 根据用户ID获取模板列表
     * @param userId 用户ID
     * @return 模板列表
     */
    @Override
    public List<Template> getTemplatesByUserId(String userId) {
        return templateMapper.selectByUserId(userId);
    }

    /**
     * 添加模板
     * @param template 模板
     * @return 模板
     */
    @Override
    public Template addTemplate(Template template) {
        template.setId(UUID.randomUUID().toString());
        templateMapper.insert(template);
        return template;
    }

    /**
     * 更新模板
     * @param template 模板
     * @return 模板
     */
    @Override
    public Template updateTemplate(Template template) {
        templateMapper.update(template);
        return template;
    }

    /**
     * 删除模板
     * @param id 模板ID
     * @return 删除数量
     */
    @Override
    public int deleteTemplateById(String id) {
        return templateMapper.deleteById(id);
    }
}
