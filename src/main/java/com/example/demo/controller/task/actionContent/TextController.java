package com.example.demo.controller.task.actionContent;

import com.example.demo.pojo.Result;
import com.example.demo.pojo.task.actionContent.Text;
import com.example.demo.service.task.actionContent.TextService;
import com.example.demo.utils.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/action-content/text")
public class TextController {

    private final TextService textService;
    private final AuthUtil authUtil;

    @Autowired
    public TextController(TextService textService, AuthUtil authUtil) {
        this.textService = textService;
        this.authUtil = authUtil;
    }

    /**
     * 获取当前用户的所有文本内容
     * @param request HTTP请求
     * @return 文本内容列表
     */
    @RequestMapping("/list")
    public Result<List<Text>> getTextsByCurrentUser(HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        List<Text> texts = textService.getTextsByUserId(userId);
        return Result.success(texts);
    }

    /**
     * 根据ID获取文本内容
     * @param id 文本内容ID
     * @param request HTTP请求
     * @return 文本内容
     */
    @RequestMapping("/{id}")
    public Result<Text> getTextById(@PathVariable String id, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        Text text = textService.getTextById(id);
        if (text == null) {
            return Result.error("文本内容不存在");
        }
        
        // 验证权限，确保用户只能访问自己的文本内容
        if (!userId.equals(text.getUserId())) {
            return Result.error("无权限访问该文本内容");
        }
        
        return Result.success(text);
    }

    /**
     * 创建文本内容
     * @param text 文本内容
     * @param request HTTP请求
     * @return 文本内容
     */
    @RequestMapping("/add")
    public Result<Text> addText(@RequestBody Text text, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        // 设置当前用户ID
        text.setUserId(userId);
        textService.addText(text);
        return Result.success();
    }

    /**
     * 更新文本内容
     * @param id 文本内容ID
     * @param text 文本内容
     * @param request HTTP请求
     * @return 文本内容
     */
    @RequestMapping("/update/{id}")
    public Result<Text> updateText(@PathVariable String id, @RequestBody Text text, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        // 验证权限，确保用户只能更新自己的文本内容
        Text existingText = textService.getTextById(id);
        if (existingText == null) {
            return Result.error("文本内容不存在");
        }
        if (!userId.equals(existingText.getUserId())) {
            return Result.error("无权限更新该文本内容");
        }
        
        // 设置当前用户ID和ID
        text.setUserId(userId);
        text.setId(id);
        Text updatedText = textService.updateText(text);
        return Result.success("文本内容更新成功", updatedText);
    }

    /**
     * 删除文本内容
     * @param id 文本内容ID
     * @param request HTTP请求
     * @return 删除结果
     */
    @RequestMapping("/delete/{id}")
    public Result<String> deleteTextById(@PathVariable String id, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        // 验证权限，确保用户只能删除自己的文本内容
        Text existingText = textService.getTextById(id);
        if (existingText == null) {
            return Result.error("文本内容不存在");
        }
        if (!userId.equals(existingText.getUserId())) {
            return Result.error("无权限删除该文本内容");
        }
        
        int result = textService.deleteTextById(id);
        if (result > 0) {
            return Result.success("文本内容删除成功");
        } else {
            return Result.error("文本内容删除失败");
        }
    }

}