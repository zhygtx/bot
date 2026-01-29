package com.example.demo.controller.task.actionContent;

import com.example.demo.pojo.Result;
import com.example.demo.pojo.task.actionContent.Ai;
import com.example.demo.service.task.actionContent.AiService;
import com.example.demo.utils.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/action-content/ai")
public class AiController {

    private final AiService aiService;
    private final AuthUtil authUtil;

    @Autowired
    public AiController(AiService aiService, AuthUtil authUtil) {
        this.aiService = aiService;
        this.authUtil = authUtil;
    }

    /**
     * 获取当前用户的所有AI配置
     * @param request HTTP请求
     * @return AI配置列表
     */
    @RequestMapping("/list")
    public Result<List<Ai>> getAisByCurrentUser(HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        List<Ai> ais = aiService.getAisByUserId(userId);
        return Result.success(ais);
    }

    /**
     * 根据ID获取AI配置
     * @param id AI配置ID
     * @param request HTTP请求
     * @return AI配置
     */
    @RequestMapping("/{id}")
    public Result<Ai> getAiById(@PathVariable String id, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        Ai ai = aiService.getAiById(id);
        if (ai == null) {
            return Result.error("AI配置不存在");
        }
        
        // 验证权限，确保用户只能访问自己的AI配置
        if (!userId.equals(ai.getUserId())) {
            return Result.error("无权限访问该AI配置");
        }
        
        return Result.success(ai);
    }

    /**
     * 创建AI配置
     * @param ai AI配置
     * @param request HTTP请求
     * @return AI配置
     */
    @RequestMapping("/add")
    public Result<Ai> addAi(@RequestBody Ai ai, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        // 设置当前用户ID
        ai.setUserId(userId);
        Ai addedAi = aiService.addAi(ai);
        return Result.success("AI配置添加成功", addedAi);
    }

    /**
     * 更新AI配置
     * @param id AI配置ID
     * @param ai AI配置
     * @param request HTTP请求
     * @return AI配置
     */
    @RequestMapping("/update/{id}")
    public Result<Ai> updateAi(@PathVariable String id, @RequestBody Ai ai, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        // 验证权限，确保用户只能更新自己的AI配置
        Ai existingAi = aiService.getAiById(id);
        if (existingAi == null) {
            return Result.error("AI配置不存在");
        }
        if (!userId.equals(existingAi.getUserId())) {
            return Result.error("无权限更新该AI配置");
        }
        
        // 设置当前用户ID和ID
        ai.setUserId(userId);
        ai.setId(id);
        Ai updatedAi = aiService.updateAi(ai);
        return Result.success("AI配置更新成功", updatedAi);
    }

    /**
     * 删除AI配置
     * @param id AI配置ID
     * @param request HTTP请求
     * @return 删除结果
     */
    @RequestMapping("/delete/{id}")
    public Result<String> deleteAiById(@PathVariable String id, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        // 验证权限，确保用户只能删除自己的AI配置
        Ai existingAi = aiService.getAiById(id);
        if (existingAi == null) {
            return Result.error("AI配置不存在");
        }
        if (!userId.equals(existingAi.getUserId())) {
            return Result.error("无权限删除该AI配置");
        }
        
        int result = aiService.deleteAiById(id);
        if (result > 0) {
            return Result.success("AI配置删除成功");
        } else {
            return Result.error("AI配置删除失败");
        }
    }

}