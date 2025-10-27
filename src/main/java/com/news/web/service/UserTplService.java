package com.news.web.service;

import com.news.web.pojo.UserTpl;

import java.util.List;

public interface UserTplService {

    int insert(UserTpl userTpl);

    Boolean hasUserTpl(String id);

    List<UserTpl> selectByUserId(Long userId);

    void update(UserTpl userTpl);

    void delete(UserTpl userTpl);
}