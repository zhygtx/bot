package com.news.web.service.impl;

import com.news.web.mapper.UserMapper;
import com.news.web.pojo.User;
import com.news.web.service.UserService;
import com.news.web.utils.Md5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public int insertUser(String username,String password){
        User user = new User();
        user.setUsername(username);
        user.setPassword(Md5Util.getMD5String(password));
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        return userMapper.insert(user);
    }

    @Override
    public User selectByName(String username) {
        return userMapper.selectByName(username);
    }


    @Override
    public int updatePwd(User user) {
        return userMapper.updatePwd(user);
    }



}