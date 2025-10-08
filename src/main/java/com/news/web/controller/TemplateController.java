package com.news.web.controller;

import com.news.web.pojo.Result;
import com.news.web.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/template")
public class TemplateController {

    @Autowired
    private TemplateService templateService;


    /**
     * 获取所有模板图片（批量接口）
     */
    @GetMapping("/all")
    public Result<Map<String, String>> getAllTemplateImages() {
        try {
            Map<String, String> templateImages = templateService.getAllTemplateImages();
            return Result.success(templateImages);
        } catch (Exception e) {
            return Result.error("获取模板图片失败: " + e.getMessage());
        }
    }
}
