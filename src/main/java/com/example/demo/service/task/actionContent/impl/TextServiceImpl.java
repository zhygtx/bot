package com.example.demo.service.task.actionContent.impl;

import com.example.demo.pojo.task.actionContent.Text;
import com.example.demo.service.task.actionContent.TextService;
import org.springframework.stereotype.Service;

@Service
public class TextServiceImpl implements TextService {

    @Override
    public Text getText(String id){
        // TODO: 从数据库获取文本细节
        return null;
    }

}
