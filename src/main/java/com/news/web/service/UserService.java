package com.news.web.service;

import com.news.web.pojo.User;

public interface UserService {

    int insertUser(String username, String password);

    User selectByName(String username);

    int updatePwd(User user);
}