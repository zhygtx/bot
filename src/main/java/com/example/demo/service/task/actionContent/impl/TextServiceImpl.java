package com.example.demo.service.task.actionContent.impl;

import com.example.demo.mapper.task.actionContent.TextMapper;
import com.example.demo.pojo.task.actionContent.Text;
import com.example.demo.service.task.actionContent.TextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

}
