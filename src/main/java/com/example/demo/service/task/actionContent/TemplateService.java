package com.example.demo.service.task.actionContent;

import com.example.demo.pojo.task.actionContent.Template;

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

}
