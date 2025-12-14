package com.example.demo.service.task.actionContent;

import com.example.demo.pojo.task.actionContent.Text;

public interface TextService {

    /**
     * 获取文本动作细节
     * @param id 动作ID
     * @return 文本动作细节
     */
    Text getText(String id);

}
