package com.example.demo.controller.task.actionContent;

import com.example.demo.pojo.Result;
import com.example.demo.pojo.task.actionContent.Template;
import com.example.demo.service.task.actionContent.TemplateService;
import com.example.demo.utils.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/action-content/template")
public class TemplateController {

    private final TemplateService templateService;
    private final AuthUtil authUtil;

    @Autowired
    public TemplateController(TemplateService templateService, AuthUtil authUtil) {
        this.templateService = templateService;
        this.authUtil = authUtil;
    }

    /**
     * 获取当前用户的所有模板
     * @param request HTTP请求
     * @return 模板列表
     */
    @RequestMapping("/list")
    public Result<List<Template>> getTemplatesByCurrentUser(HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        List<Template> templates = templateService.getTemplatesByUserId(userId);
        return Result.success(templates);
    }

    /**
     * 根据ID获取模板
     * @param id 模板ID
     * @param request HTTP请求
     * @return 模板
     */
    @RequestMapping("/{id}")
    public Result<Template> getTemplateById(@PathVariable String id, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        Template template = templateService.getTemplateById(id);
        if (template == null) {
            return Result.error("模板不存在");
        }
        
        // 验证权限，确保用户只能访问自己的模板
        if (!userId.equals(template.getUserId())) {
            return Result.error("无权限访问该模板");
        }
        
        return Result.success(template);
    }

    /**
     * 创建模板
     * @param template 模板
     * @param request HTTP请求
     * @return 模板
     */
    @RequestMapping("/add")
    public Result<Template> addTemplate(@RequestBody Template template, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        // 设置当前用户ID
        template.setUserId(userId);
        Template addedTemplate = templateService.addTemplate(template);
        return Result.success("模板添加成功", addedTemplate);
    }

    /**
     * 更新模板
     * @param id 模板ID
     * @param template 模板
     * @param request HTTP请求
     * @return 模板
     */
    @RequestMapping("/update/{id}")
    public Result<Template> updateTemplate(@PathVariable String id, @RequestBody Template template, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        // 验证权限，确保用户只能更新自己的模板
        Template existingTemplate = templateService.getTemplateById(id);
        if (existingTemplate == null) {
            return Result.error("模板不存在");
        }
        if (!userId.equals(existingTemplate.getUserId())) {
            return Result.error("无权限更新该模板");
        }
        
        // 设置当前用户ID和ID
        template.setUserId(userId);
        template.setId(id);
        Template updatedTemplate = templateService.updateTemplate(template);
        return Result.success("模板更新成功", updatedTemplate);
    }

    /**
     * 删除模板
     * @param id 模板ID
     * @param request HTTP请求
     * @return 删除结果
     */
    @RequestMapping("/delete/{id}")
    public Result<String> deleteTemplateById(@PathVariable String id, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        // 验证权限，确保用户只能删除自己的模板
        Template existingTemplate = templateService.getTemplateById(id);
        if (existingTemplate == null) {
            return Result.error("模板不存在");
        }
        if (!userId.equals(existingTemplate.getUserId())) {
            return Result.error("无权限删除该模板");
        }
        
        int result = templateService.deleteTemplateById(id);
        if (result > 0) {
            return Result.success("模板删除成功");
        } else {
            return Result.error("模板删除失败");
        }
    }

}