package com.example.demo.controller.task.actionContent;

import com.example.demo.pojo.Result;
import com.example.demo.pojo.task.actionContent.Api;
import com.example.demo.service.task.actionContent.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/action-content/api")
public class ApiController {

    private final ApiService apiService;

    @Autowired
    public ApiController(ApiService apiService) {
        this.apiService = apiService;
    }

    /**
     * 获取所有API调用内容
     * @return API调用内容列表
     */
    @GetMapping
    public Result<List<Api>> getAllApis() {
        return Result.success(apiService.getAllApis());
    }

    /**
     * 根据ID获取API调用内容
     * @param id API调用内容ID
     * @return API调用内容
     */
    @GetMapping("/{id}")
    public Result<Api> getApiById(@PathVariable String id) {
        Api api = apiService.getApiById(id);
        if (api == null) {
            return Result.error("API调用内容不存在");
        }
        return Result.success(api);
    }

    /**
     * 根据用户ID获取API调用内容列表
     * @param userId 用户ID
     * @return API调用内容列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<Api>> getApisByUserId(@PathVariable String userId) {
        return Result.success(apiService.getApisByUserId(userId));
    }

    /**
     * 创建API调用内容
     * @param api API调用内容
     * @return API调用内容
     */
    @PostMapping
    public Result<Api> addApi(@RequestBody Api api) {
        Api addedApi = apiService.addApi(api);
        return Result.success("API调用内容添加成功", addedApi);
    }

    /**
     * 更新API调用内容
     * @param id API调用内容ID
     * @param api API调用内容
     * @return API调用内容
     */
    @PutMapping("/{id}")
    public Result<Api> updateApi(@PathVariable String id, @RequestBody Api api) {
        api.setId(id);
        Api updatedApi = apiService.updateApi(api);
        return Result.success("API调用内容更新成功", updatedApi);
    }

    /**
     * 删除API调用内容
     * @param id API调用内容ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteApiById(@PathVariable String id) {
        int result = apiService.deleteApiById(id);
        if (result > 0) {
            return Result.success("API调用内容删除成功");
        } else {
            return Result.error("API调用内容删除失败");
        }
    }

}