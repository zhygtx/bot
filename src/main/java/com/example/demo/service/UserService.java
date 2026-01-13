package com.example.demo.service;

import com.example.demo.pojo.User;

/**
 * 用户类服务接口
 */
public interface UserService {

    /**
     * 判断用户是否存在
     * @param userAccount 用户ID
     * @return 存在返回true
     */
    Boolean isExistByAccount(String userAccount);

    /**
     * 判断用户是否存在
     * @param email 用户邮箱
     * @return 存在返回true
     */
    Boolean isExistByEmail(String email);

    /**
     * 根据用户账号查询用户信息
     * @param account 用户ID
     * @return 用户信息
     */
    User selectByAccount(String account);

    /**
     * 根据用户ID查询用户信息
     * @param id 用户ID
     * @return 用户信息
     */
    User selectById(String id);

    /**
     * 插入用户
     * @param user 用户信息
     */
    void insertUser(User user);

    /**
     * 登录
     * @param account 用户ID
     * @param pwd 密码
     * @return 登录成功返回true
     */
    Boolean login(String account, String pwd);

    /**
     * 修改用户信息
     * @param user 用户信息
     */
    void updateUser(User user,String userId);

    /**
     * 修改密码
     * @param user 用户信息
     */
    void updatePwd(User user);

    /**
     * 修改邮箱
     * @param user 用户信息
     */
    void updateEmail(User user);

    /**
     * 获取用户邮箱
     * @param id 用户ID
     * @return 邮箱
     */
    String selectEmail(String id);

    /**
     * 获取用户邮箱
     * @param botQQ 机器人QQ
     * @return 邮箱
     */
    String selectEmail(Long botQQ);
}