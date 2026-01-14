package com.example.demo.service.task.actionContent;

import com.example.demo.pojo.task.actionContent.Text;

import java.util.List;

public interface TextService {

    /**
     * 获取文本动作细节
     * @param id 动作ID
     * @return 文本动作细节
     */
    Text getText(String id);

    /**
     * 获取所有文本内容
     * @return 文本内容列表
     */
    List<Text> getAllTexts();

    /**
     * 根据ID获取文本内容
     * @param id 文本内容ID
     * @return 文本内容
     */
    Text getTextById(String id);

    /**
     * 根据用户ID获取文本内容列表
     * @param userId 用户ID
     * @return 文本内容列表
     */
    List<Text> getTextsByUserId(String userId);

    /**
     * 添加文本内容
     *
     * @param text 文本内容
     */
    void addText(Text text);

    /**
     * 更新文本内容
     * @param text 文本内容
     * @return 文本内容
     */
    Text updateText(Text text);

    /**
     * 删除文本内容
     * @param id 文本内容ID
     * @return 删除数量
     */
    int deleteTextById(String id);

}
