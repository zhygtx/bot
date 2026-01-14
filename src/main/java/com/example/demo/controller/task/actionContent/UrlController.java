package com.example.demo.controller.task.actionContent;

import com.example.demo.pojo.Result;
import com.example.demo.pojo.task.actionContent.Url;
import com.example.demo.service.task.actionContent.UrlService;
import com.example.demo.utils.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/action-content/url")
public class UrlController {

    private final UrlService urlService;
    private final AuthUtil authUtil;

    @Autowired
    public UrlController(UrlService urlService, AuthUtil authUtil) {
        this.urlService = urlService;
        this.authUtil = authUtil;
    }

    /**
     * 获取当前用户的所有URL
     * @param request HTTP请求
     * @return URL列表
     */
    @RequestMapping("/list")
    public Result<List<Url>> getUrlsByCurrentUser(HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        List<Url> urls = urlService.getUrlsByUserId(userId);
        return Result.success(urls);
    }

    /**
     * 根据ID获取URL
     * @param id URL ID
     * @param request HTTP请求
     * @return URL
     */
    @RequestMapping("/{id}")
    public Result<Url> getUrlById(@PathVariable String id, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        Url url = urlService.getUrl(id);
        if (url == null) {
            return Result.error("URL不存在");
        }
        
        // 验证权限，确保用户只能访问自己的URL
        if (!userId.equals(url.getUserId())) {
            return Result.error("无权限访问该URL");
        }
        
        return Result.success(url);
    }

    /**
     * 创建URL
     * @param url URL
     * @param request HTTP请求
     * @return URL
     */
    @RequestMapping("/add")
    public Result<Url> addUrl(@RequestBody Url url, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        // 设置当前用户ID
        url.setUserId(userId);
        Url addedUrl = urlService.addUrl(url);
        return Result.success("URL添加成功", addedUrl);
    }

    /**
     * 更新URL
     * @param id URL ID
     * @param url URL
     * @param request HTTP请求
     * @return URL
     */
    @RequestMapping("/update/{id}")
    public Result<Url> updateUrl(@PathVariable String id, @RequestBody Url url, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        // 验证权限，确保用户只能更新自己的URL
        Url existingUrl = urlService.getUrl(id);
        if (existingUrl == null) {
            return Result.error("URL不存在");
        }
        if (!userId.equals(existingUrl.getUserId())) {
            return Result.error("无权限更新该URL");
        }
        
        // 设置当前用户ID和ID
        url.setUserId(userId);
        url.setId(id);
        Url updatedUrl = urlService.updateUrl(url);
        return Result.success("URL更新成功", updatedUrl);
    }

    /**
     * 删除URL
     * @param id URL ID
     * @param request HTTP请求
     * @return 删除结果
     */
    @RequestMapping("/delete/{id}")
    public Result<String> deleteUrlById(@PathVariable String id, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        // 验证权限，确保用户只能删除自己的URL
        Url existingUrl = urlService.getUrl(id);
        if (existingUrl == null) {
            return Result.error("URL不存在");
        }
        if (!userId.equals(existingUrl.getUserId())) {
            return Result.error("无权限删除该URL");
        }
        
        int result = urlService.deleteUrlById(id);
        if (result > 0) {
            return Result.success("URL删除成功");
        } else {
            return Result.error("URL删除失败");
        }
    }

}