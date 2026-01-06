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
    Boolean isExist(String userAccount);

    /**
     * 根据用户账号查询用户信息
     * @param account 用户ID
     * @return 用户信息
     */
    User selectByAccount(String account);

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
    void updateUser(User user);

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
}
