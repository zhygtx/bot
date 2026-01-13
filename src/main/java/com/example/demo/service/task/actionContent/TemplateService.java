package com.example.demo.service.task.actionContent;

import com.example.demo.pojo.task.actionContent.Template;

import java.util.List;

/**
 * 模板服务接口
 */
public interface TemplateService {

    /**
     * 根据ID获取模板
     * @param id 模板ID
     * @return 模板
     */
    Template getById(String id);

    /**
     * 获取所有模板
     * @return 模板列表
     */
    List<Template> getAllTemplates();

    /**
     * 根据ID获取模板
     * @param id 模板ID
     * @return 模板
     */
    Template getTemplateById(String id);

    /**
     * 根据用户ID获取模板列表
     * @param userId 用户ID
     * @return 模板列表
     */
    List<Template> getTemplatesByUserId(String userId);

    /**
     * 添加模板
     * @param template 模板
     * @return 模板
     */
    Template addTemplate(Template template);

    /**
     * 更新模板
     * @param template 模板
     * @return 模板
     */
    Template updateTemplate(Template template);

    /**
     * 删除模板
     * @param id 模板ID
     * @return 删除数量
     */
    int deleteTemplateById(String id);

}
