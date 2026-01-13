package com.example.demo.controller.task.actionContent;

import com.example.demo.pojo.Result;
import com.example.demo.pojo.task.actionContent.Text;
import com.example.demo.service.task.actionContent.TextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/action-content/text")
public class TextController {

    private final TextService textService;

    @Autowired
    public TextController(TextService textService) {
        this.textService = textService;
    }

    /**
     * 获取所有文本内容
     * @return 文本内容列表
     */
    @GetMapping
    public Result<List<Text>> getAllTexts() {
        return Result.success(textService.getAllTexts());
    }

    /**
     * 根据ID获取文本内容
     * @param id 文本内容ID
     * @return 文本内容
     */
    @GetMapping("/{id}")
    public Result<Text> getTextById(@PathVariable String id) {
        Text text = textService.getTextById(id);
        if (text == null) {
            return Result.error("文本内容不存在");
        }
        return Result.success(text);
    }

    /**
     * 根据用户ID获取文本内容列表
     * @param userId 用户ID
     * @return 文本内容列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<Text>> getTextsByUserId(@PathVariable String userId) {
        return Result.success(textService.getTextsByUserId(userId));
    }

    /**
     * 创建文本内容
     * @param text 文本内容
     * @return 文本内容
     */
    @PostMapping
    public Result<Text> addText(@RequestBody Text text) {
        Text addedText = textService.addText(text);
        return Result.success("文本内容添加成功", addedText);
    }

    /**
     * 更新文本内容
     * @param id 文本内容ID
     * @param text 文本内容
     * @return 文本内容
     */
    @PutMapping("/{id}")
    public Result<Text> updateText(@PathVariable String id, @RequestBody Text text) {
        text.setId(id);
        Text updatedText = textService.updateText(text);
        return Result.success("文本内容更新成功", updatedText);
    }

    /**
     * 删除文本内容
     * @param id 文本内容ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteTextById(@PathVariable String id) {
        int result = textService.deleteTextById(id);
        if (result > 0) {
            return Result.success("文本内容删除成功");
        } else {
            return Result.error("文本内容删除失败");
        }
    }

}