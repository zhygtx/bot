package com.example.demo.controller.task.actionContent;

import com.example.demo.pojo.Result;
import com.example.demo.pojo.task.actionContent.Template;
import com.example.demo.service.task.actionContent.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/action-content/template")
public class TemplateController {

    private final TemplateService templateService;

    @Autowired
    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    /**
     * 获取所有模板
     * @return 模板列表
     */
    @GetMapping
    public Result<List<Template>> getAllTemplates() {
        return Result.success(templateService.getAllTemplates());
    }

    /**
     * 根据ID获取模板
     * @param id 模板ID
     * @return 模板
     */
    @GetMapping("/{id}")
    public Result<Template> getTemplateById(@PathVariable String id) {
        Template template = templateService.getTemplateById(id);
        if (template == null) {
            return Result.error("模板不存在");
        }
        return Result.success(template);
    }

    /**
     * 根据用户ID获取模板列表
     * @param userId 用户ID
     * @return 模板列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<Template>> getTemplatesByUserId(@PathVariable String userId) {
        return Result.success(templateService.getTemplatesByUserId(userId));
    }

    /**
     * 创建模板
     * @param template 模板
     * @return 模板
     */
    @PostMapping
    public Result<Template> addTemplate(@RequestBody Template template) {
        Template addedTemplate = templateService.addTemplate(template);
        return Result.success("模板添加成功", addedTemplate);
    }

    /**
     * 更新模板
     * @param id 模板ID
     * @param template 模板
     * @return 模板
     */
    @PutMapping("/{id}")
    public Result<Template> updateTemplate(@PathVariable String id, @RequestBody Template template) {
        template.setId(id);
        Template updatedTemplate = templateService.updateTemplate(template);
        return Result.success("模板更新成功", updatedTemplate);
    }

    /**
     * 删除模板
     * @param id 模板ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteTemplateById(@PathVariable String id) {
        int result = templateService.deleteTemplateById(id);
        if (result > 0) {
            return Result.success("模板删除成功");
        } else {
            return Result.error("模板删除失败");
        }
    }

}