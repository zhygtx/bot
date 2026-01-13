package com.example.demo.controller.task.actionContent;

import com.example.demo.pojo.Result;
import com.example.demo.pojo.task.actionContent.Url;
import com.example.demo.service.task.actionContent.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/action-content/url")
public class UrlController {

    private final UrlService urlService;

    @Autowired
    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    /**
     * 获取所有URL
     * @return URL列表
     */
    @GetMapping
    public Result<List<Url>> getAllUrls() {
        return Result.success(urlService.getAllUrls());
    }

    /**
     * 根据ID获取URL
     * @param id URL ID
     * @return URL
     */
    @GetMapping("/{id}")
    public Result<Url> getUrlById(@PathVariable String id) {
        Url url = urlService.getUrl(id);
        if (url == null) {
            return Result.error("URL不存在");
        }
        return Result.success(url);
    }

    /**
     * 根据用户ID获取URL列表
     * @param userId 用户ID
     * @return URL列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<Url>> getUrlsByUserId(@PathVariable String userId) {
        return Result.success(urlService.getUrlsByUserId(userId));
    }

    /**
     * 创建URL
     * @param url URL
     * @return URL
     */
    @PostMapping
    public Result<Url> addUrl(@RequestBody Url url) {
        Url addedUrl = urlService.addUrl(url);
        return Result.success("URL添加成功", addedUrl);
    }

    /**
     * 更新URL
     * @param id URL ID
     * @param url URL
     * @return URL
     */
    @PutMapping("/{id}")
    public Result<Url> updateUrl(@PathVariable String id, @RequestBody Url url) {
        url.setId(id);
        Url updatedUrl = urlService.updateUrl(url);
        return Result.success("URL更新成功", updatedUrl);
    }

    /**
     * 删除URL
     * @param id URL ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteUrlById(@PathVariable String id) {
        int result = urlService.deleteUrlById(id);
        if (result > 0) {
            return Result.success("URL删除成功");
        } else {
            return Result.error("URL删除失败");
        }
    }

}