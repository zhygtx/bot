package com.news.web.service.impl;

import com.news.web.mapper.UserTplMapper;
import com.news.web.pojo.UserTpl;
import com.news.web.service.UserTplService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserTplServiceImpl implements UserTplService {

    @Autowired
    private UserTplMapper userTplMapper;

    @Override
    public int insert(UserTpl userTpl) {
        return userTplMapper.insert(userTpl);
    }

    @Override
    public UserTpl selectByUserId(Long userId){
        return userTplMapper.selectByUserId(userId);
    }

    @Override
    public void update(UserTpl userTpl){
        userTplMapper.update(userTpl);
    }

}
