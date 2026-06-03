package com.example.demo.service.impl;

import com.example.demo.mapper.UserMapper;
import com.example.demo.pojo.entity.User;
import com.example.demo.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

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
    public Boolean isExistByAccount(String userAccount) {
        return userMapper.isExistByAccount(userAccount);
    }

    /**
     * 判断用户是否存在
     * @param email 邮箱
     * @return 存在返回true
     */
    @Override
    public Boolean isExistByEmail(String email) {
        return userMapper.isExistByEmail(email);
    }

    /**
     * 根据用户ID查询用户信息
     * @param account 用户ID
     * @return 用户信息
     */
    @Override
    public User selectByAccount(String account) {
        return userMapper.selectByAccount(account);
    }

    /**
     * 根据用户ID查询用户信息
     * @param id 用户ID
     * @return 用户信息
     */
    @Override
    public User selectById(String id) {
        return userMapper.selectById(id);
    }

    /**
     * 插入用户
     * @param user 用户信息
     */
    @Override
    @Transactional
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
    @Transactional
    public Integer updateUser(User user){
        return userMapper.updateUser(user);
    }

    /**
     * 修改密码
     * @param user 用户信息
     */
    @Override
    @Transactional
    public void updatePwd(User user){
        user.setUpdateTime(LocalDateTime.now());
        user.setPwd(passwordEncoder.encode(user.getPwd()));
        userMapper.updatePwd(user);
    }

    /**
     * 修改邮箱
     * @param user 用户信息
     */
    @Override
    @Transactional
    public void updateEmail(User user) {
        user.setAccount(user.getAccount());
        user.setEmail(user.getEmail());
        userMapper.updateEmail(user);
    }

    /**
     * 根据用户ID查询邮箱
     * @param id 用户ID
     * @return 邮箱
     */
    @Override
    public String selectEmail(String id) {
        return userMapper.selectEmailById(id);
    }

    /**
     * 根据botQQ查询邮箱
     * @param botQQ botQQ
     * @return 邮箱
     */
    @Override
    public String selectEmail(Long botQQ) {
        return userMapper.selectEmailByBotQQ(botQQ);
    }
}