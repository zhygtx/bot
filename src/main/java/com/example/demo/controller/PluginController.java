package com.example.demo.controller;

import com.example.demo.pojo.Result;
import com.example.demo.pojo.plugin.PluginInfo;
import com.example.demo.service.PluginService;
import com.example.demo.util.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/plugin")
@Slf4j
public class PluginController {

    private final PluginService pluginService;
    private final AuthUtil authUtil;

    public PluginController(PluginService pluginService, AuthUtil authUtil) {
        this.pluginService = pluginService;
        this.authUtil = authUtil;
    }

    /**
     * 添加插件
     * @param request 请求对象
     * @param pluginInfo 插件信息
     * @param file 插件文件
     * @return 添加结果
     */
    @PostMapping
    public Result<?> add(HttpServletRequest request,
                         @RequestPart("pluginInfo") PluginInfo pluginInfo,
                         @RequestPart("file") MultipartFile file) {
        String userId = authUtil.getCurrentUserId(request);
        String userName = authUtil.getCurrentUserName(request);
        pluginInfo.setAuthorName(userName);
        pluginInfo.setAuthorId(userId);
        return pluginService.add(pluginInfo, file);
    }

    /**
     * 删除插件
     * @param id 插件id
     * @return 删除结果
     */
    @DeleteMapping
    public Result<?> delete(@RequestParam String id) {
        int sqlResult = pluginService.remove(id);
        return sqlResult == 0 ? Result.error(500, "删除插件失败") : Result.success();
    }

    /**
     * 修改插件信息
     * @param pluginInfo 插件信息
     * @return 修改结果
     */
    @PutMapping
    public Result<?> edit(@RequestBody PluginInfo pluginInfo) {
        int sqlResult = pluginService.edit(pluginInfo);
        return sqlResult == 0 ? Result.error(500, "修改插件失败") : Result.success(null,null);
    }

    @PutMapping("/editPublic")
    public Result<?> editPublic() {
        return Result.success(null,null);
    }

    /**
     * 根据id查询插件信息
     * @param id 插件id
     * @return 插件信息
     */
    @GetMapping("/{id}")
    public Result<?> findById(@PathVariable("id") String id) {
        return Result.success(null,pluginService.findById(id));
    }

    /**
     * 获取插件列表
     * @param request 请求对象
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 插件列表
     */
    @GetMapping("/findByAuthorId")
    public Result<?> findByAuthorId(HttpServletRequest request,
                                    @RequestParam(required = false,defaultValue = "1") int pageNum,
                                    @RequestParam(required = false,defaultValue = "12") int pageSize) {
        String userId = authUtil.getCurrentUserId(request);
        return Result.success(null,pluginService.findByAuthorId(userId, pageNum, pageSize));
    }
}
