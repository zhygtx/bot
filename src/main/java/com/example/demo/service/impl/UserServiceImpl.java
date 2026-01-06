package com.example.demo.service.impl;

import com.example.demo.mapper.UserMapper;
import com.example.demo.pojo.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 判断用户是否存在
     * @param userAccount 用户ID
     * @return 存在返回true
     */
    @Override
    public Boolean isExist(String userAccount) {
        return userMapper.isExist(userAccount);
    }

    /**
     * 插入用户
     * @param user 用户信息
     */
    @Override
    public void insertUser(User user) {
        user.setId(UUID.randomUUID().toString());
        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        String encodedPassword = passwordEncoder.encode(user.getPwd());
        user.setPwd(encodedPassword);
        userMapper.insertUser(user);
    }

    /**
     * 登录
     * @param account 用户ID
     * @param pwd 密码
     * @return 登录成功返回true
     */
    @Override
    public Boolean login(String account, String pwd){
        User user = userMapper.selectByAccount(account);
        return user != null && passwordEncoder.matches(pwd, user.getPwd());
    }

    /**
     * 更新用户信息
     * @param user 用户信息
     */
    @Override
    public void updateUser(User user){
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateUser(user);
    }

    /**
     * 修改密码
     * @param user 用户信息
     */
    @Override
    public void updatePwd(User user){
        user.setUpdateTime(LocalDateTime.now());
        user.setPwd(passwordEncoder.encode(user.getPwd()));
        userMapper.updatePwd(user);
    }
}
