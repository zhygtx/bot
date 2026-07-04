package com.example.demo.controller;

import com.example.demo.pojo.entity.Result;
import com.example.demo.pojo.entity.plugin.PluginInfo;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.PluginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/plugin")
@Slf4j
public class PluginController {

    private final PluginService pluginService;

    public PluginController(PluginService pluginService) {
        this.pluginService = pluginService;
    }

    /**
     * 添加插件
     * @param request 请求对象
     * @param pluginInfo 插件信息
     * @param file 插件文件
     * @return 添加结果
     */
    @PostMapping
    public Result<?> add(@AuthenticationPrincipal UserPrincipal user,
                         @RequestPart("pluginInfo") PluginInfo pluginInfo,
                         @RequestPart("file") MultipartFile file) {
        String userId = user.userId();
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
        return sqlResult == 0 ? Result.error(500, "删除插件失败") : Result.success(null, null);
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

    /**
     * 修改插件公开状态
     * @param id 插件id
     * @param isPublic 公开状态
     * @return 修改结果
     */
    @PutMapping("/editPublic")
    public Result<?> editPublic(String id, boolean isPublic) {
        int sqlResult = pluginService.editPublic(id, isPublic);
        return sqlResult == 0 ? Result.error(500, "修改插件失败") : Result.success(null,null);
    }

    /**
     * 根据id查询插件信息
     * @param pluginId 插件id
     * @param pluginVersionId 插件版本id
     * @return 插件信息
     */
    @GetMapping("/findPlugin")
    public Result<?> findPlugin(String pluginId, String pluginVersionId) {
        return Result.success(null,pluginService.findByPluginIdAndVersionId(pluginId, pluginVersionId));
    }

    /**
     * 获取插件列表
      * @param content 模糊查询内容
     * @param authorId 插件作者id
     * @param isPublic 插件是否公开
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 插件列表
     */
    @GetMapping("/findPlugins")
    public Result<?> findPlugins(@RequestParam(required = false) String content,
                                 @RequestParam(required = false) String authorId,
                                 @RequestParam(required = false) Boolean isPublic,
                                 @RequestParam(required = false, defaultValue = "1") int pageNum,
                                 @RequestParam(required = false, defaultValue = "12") int pageSize) {
        return Result.success(null,pluginService.findPlugins(content, authorId, isPublic, pageNum, pageSize));
    }

    /**
     * 根据插件id查询插件版本信息
     * @param pluginId 插件id
     * @return 插件版本信息
     */
    @GetMapping("/findPluginVersionByPluginId")
    public Result<?> findPluginVersionByPluginId(String pluginId) {
        return Result.success(null,pluginService.findPluginVersionByPluginId(pluginId));
    }
}
