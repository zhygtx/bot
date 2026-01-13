package com.example.demo.controller.task;

import com.example.demo.pojo.Result;
import com.example.demo.pojo.task.ExtractPosition;
import com.example.demo.service.task.ExtractPositionService;
import com.example.demo.utils.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/extract-position")
public class ExtractPositionController {

    private final ExtractPositionService extractPositionService;
    private final AuthUtil authUtil;

    @Autowired
    public ExtractPositionController(ExtractPositionService extractPositionService, AuthUtil authUtil) {
        this.extractPositionService = extractPositionService;
        this.authUtil = authUtil;
    }

    /**
     * 根据ID获取提取位置
     * @param id 提取位置ID
     * @param request HTTP请求
     * @return 提取位置
     */
    @RequestMapping("/{id}")
    public Result<ExtractPosition> getExtractPositionById(@PathVariable Long id, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        ExtractPosition extractPosition = extractPositionService.getExtractPositionById(id);
        if (extractPosition == null) {
            return Result.error("提取位置不存在");
        }
        return Result.success(extractPosition);
    }

    /**
     * 根据Role ID获取提取位置列表
     * @param roleId Role ID
     * @param request HTTP请求
     * @return 提取位置列表
     */
    @RequestMapping("/role/{roleId}")
    public Result<List<ExtractPosition>> getExtractPositionsByRoleId(@PathVariable String roleId, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        List<ExtractPosition> extractPositions = extractPositionService.getExtractPositionsByRoleId(roleId);
        return Result.success(extractPositions);
    }

    /**
     * 创建提取位置
     * @param extractPosition 提取位置
     * @param request HTTP请求
     * @return 提取位置
     */
    @RequestMapping("/add")
    public Result<ExtractPosition> addExtractPosition(@RequestBody ExtractPosition extractPosition, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        ExtractPosition addedExtractPosition = extractPositionService.addExtractPosition(extractPosition);
        return Result.success("提取位置添加成功", addedExtractPosition);
    }

    /**
     * 更新提取位置
     * @param id 提取位置ID
     * @param extractPosition 提取位置
     * @param request HTTP请求
     * @return 提取位置
     */
    @RequestMapping("/update/{id}")
    public Result<ExtractPosition> updateExtractPosition(@PathVariable Long id, @RequestBody ExtractPosition extractPosition, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        extractPosition.setId(id);
        ExtractPosition updatedExtractPosition = extractPositionService.updateExtractPosition(extractPosition);
        return Result.success("提取位置更新成功", updatedExtractPosition);
    }

    /**
     * 删除提取位置
     * @param id 提取位置ID
     * @param request HTTP请求
     * @return 删除结果
     */
    @RequestMapping("/delete/{id}")
    public Result<String> deleteExtractPosition(@PathVariable Long id, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        int result = extractPositionService.deleteExtractPosition(id);
        if (result > 0) {
            return Result.success("提取位置删除成功");
        } else {
            return Result.error("提取位置删除失败");
        }
    }

    /**
     * 根据Role ID删除提取位置
     * @param roleId Role ID
     * @param request HTTP请求
     * @return 删除结果
     */
    @RequestMapping("/delete/role/{roleId}")
    public Result<String> deleteExtractPositionsByRoleId(@PathVariable String roleId, HttpServletRequest request) {
        // 获取当前用户ID
        String userId = authUtil.getCurrentUserId(request);
        if (userId == null) {
            return Result.error("未授权访问");
        }
        
        int result = extractPositionService.deleteExtractPositionsByRoleId(roleId);
        return Result.success("成功删除" + result + "条提取位置");
    }

}