package com.example.demo.service.task.actionContent.impl;

import com.example.demo.mapper.task.actionContent.TextMapper;
import com.example.demo.pojo.task.actionContent.Text;
import com.example.demo.service.task.actionContent.TextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TextServiceImpl implements TextService {

    private final TextMapper textMapper;

    @Autowired
    public TextServiceImpl(TextMapper textMapper) {
        this.textMapper = textMapper;
    }

    /**
     * 根据ID获取文本细节
     * @param id 文本ID
     * @return 文本细节
     */
    @Override
    public Text getText(String id){
        return textMapper.getText(id);
    }

    /**
     * 获取所有文本内容
     * @return 文本内容列表
     */
    @Override
    public List<Text> getAllTexts() {
        return textMapper.getAllTexts();
    }

    /**
     * 根据ID获取文本内容
     * @param id 文本内容ID
     * @return 文本内容
     */
    @Override
    public Text getTextById(String id) {
        return textMapper.selectById(id);
    }

    /**
     * 根据用户ID获取文本内容列表
     * @param userId 用户ID
     * @return 文本内容列表
     */
    @Override
    public List<Text> getTextsByUserId(String userId) {
        return textMapper.selectByUserId(userId);
    }

    /**
     * 添加文本内容
     * @param text 文本内容
     * @return 文本内容
     */
    @Override
    public Text addText(Text text) {
        textMapper.insert(text);
        return text;
    }

    /**
     * 更新文本内容
     * @param text 文本内容
     * @return 文本内容
     */
    @Override
    public Text updateText(Text text) {
        textMapper.update(text);
        return text;
    }

    /**
     * 删除文本内容
     * @param id 文本内容ID
     * @return 删除数量
     */
    @Override
    public int deleteTextById(String id) {
        return textMapper.deleteById(id);
    }
}
