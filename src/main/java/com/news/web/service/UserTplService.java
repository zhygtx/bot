package com.news.web.service;

import com.news.web.pojo.UserTpl;

public interface UserTplService {

    int insert(UserTpl userTpl);

    UserTpl selectByUserId(Long userId);

    void update(UserTpl userTpl);

    void delete(UserTpl userTpl);
}