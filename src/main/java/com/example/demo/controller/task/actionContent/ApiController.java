package com.example.demo.controller.task.actionContent;

import com.example.demo.pojo.Result;
import com.example.demo.pojo.task.actionContent.Api;
import com.example.demo.service.task.actionContent.ApiService;
import com.example.demo.utils.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/action-content/api")
public class ApiController {

    private final ApiService apiService;
    private final AuthUtil authUtil;

    @Autowired
    public ApiController(ApiService apiService, AuthUtil authUtil) {
        this.apiService = apiService;
        this.authUtil = authUtil;
    }

    /**
     * 获取当前用户的所有API调用内容
     * @param request HTTP请求
     * @return API调用内容列表
     */
    @RequestMapping("/list")
    public Result<List<Api>> getApisByCurrentUser(HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        List<Api> apis = apiService.getApisByUserId(userId);
        return Result.success(apis);
    }

    /**
     * 根据ID获取API调用内容
     * @param id API调用内容ID
     * @param request HTTP请求
     * @return API调用内容
     */
    @RequestMapping("/{id}")
    public Result<Api> getApiById(@PathVariable String id, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        Api api = apiService.getApiById(id);
        if (api == null) {
            return Result.error("API调用内容不存在");
        }
        
        // 验证权限，确保用户只能访问自己的API调用内容
        if (!userId.equals(api.getUserId())) {
            return Result.error("无权限访问该API调用内容");
        }
        
        return Result.success(api);
    }

    /**
     * 创建API调用内容
     * @param api API调用内容
     * @param request HTTP请求
     * @return API调用内容
     */
    @RequestMapping("/add")
    public Result<Api> addApi(@RequestBody Api api, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        // 设置当前用户ID
        api.setUserId(userId);
        Api addedApi = apiService.addApi(api);
        return Result.success("API调用内容添加成功", addedApi);
    }

    /**
     * 更新API调用内容
     * @param id API调用内容ID
     * @param api API调用内容
     * @param request HTTP请求
     * @return API调用内容
     */
    @RequestMapping("/update/{id}")
    public Result<Api> updateApi(@PathVariable String id, @RequestBody Api api, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        // 验证权限，确保用户只能更新自己的API调用内容
        Api existingApi = apiService.getApiById(id);
        if (existingApi == null) {
            return Result.error("API调用内容不存在");
        }
        if (!userId.equals(existingApi.getUserId())) {
            return Result.error("无权限更新该API调用内容");
        }
        
        // 设置当前用户ID和ID
        api.setUserId(userId);
        api.setId(id);
        Api updatedApi = apiService.updateApi(api);
        return Result.success("API调用内容更新成功", updatedApi);
    }

    /**
     * 删除API调用内容
     * @param id API调用内容ID
     * @param request HTTP请求
     * @return 删除结果
     */
    @RequestMapping("/delete/{id}")
    public Result<String> deleteApiById(@PathVariable String id, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        // 验证权限，确保用户只能删除自己的API调用内容
        Api existingApi = apiService.getApiById(id);
        if (existingApi == null) {
            return Result.error("API调用内容不存在");
        }
        if (!userId.equals(existingApi.getUserId())) {
            return Result.error("无权限删除该API调用内容");
        }
        
        int result = apiService.deleteApiById(id);
        if (result > 0) {
            return Result.success("API调用内容删除成功");
        } else {
            return Result.error("API调用内容删除失败");
        }
    }

}